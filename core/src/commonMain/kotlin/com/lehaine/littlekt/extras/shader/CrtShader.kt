package com.lehaine.littlekt.extras.shader

import com.littlekt.file.FloatBuffer
import com.littlekt.graphics.*
import com.littlekt.graphics.shader.Shader
import com.littlekt.graphics.shader.builder.ShaderBindingType
import com.littlekt.graphics.shader.builder.ShaderStructParameterType
import com.littlekt.graphics.shader.builder.shader
import com.littlekt.graphics.shader.builder.shaderStruct
import com.littlekt.graphics.util.BindingUsage
import com.littlekt.graphics.webgpu.*
import com.littlekt.math.Vec2f
import com.littlekt.util.align

private fun shaderCode(device: Device) = shader {
    val vertexInputStruct = shaderStruct("VertexInput") {
        mapOf(
            "pos" to ShaderStructParameterType.Location(0, ShaderStructParameterType.WgslType.vec3f),
            "color" to ShaderStructParameterType.Location(1, ShaderStructParameterType.WgslType.vec4f),
            "uv" to ShaderStructParameterType.Location(2, ShaderStructParameterType.WgslType.vec2f),
        )
    }

    val vertexOutputStruct = shaderStruct("VertexOutput") {
        mapOf(
            "color" to ShaderStructParameterType.Location(0, ShaderStructParameterType.WgslType.vec4f),
            "uv" to ShaderStructParameterType.Location(1, ShaderStructParameterType.WgslType.vec2f),
            "position" to ShaderStructParameterType.BuiltIn.Position(ShaderStructParameterType.WgslType.vec4f)
        )
    }

    val fragmentOutputStruct = shaderStruct("FragmentOutput") {
        mapOf("color" to ShaderStructParameterType.Location(0, ShaderStructParameterType.WgslType.vec4f))
    }

    val cameraStruct = shaderStruct("Camera") {
        mapOf("view_proj" to ShaderStructParameterType.WgslType.mat4x4f)
    }

    val scanlineStruct = shaderStruct("Scanline") {
        mapOf(
            "curvature" to ShaderStructParameterType.WgslType.vec2f,
            "vignette" to ShaderStructParameterType.WgslType.f32,
            "alpha" to ShaderStructParameterType.WgslType.f32,
            "texel_size" to ShaderStructParameterType.WgslType.vec2f,
            "uv_scale" to ShaderStructParameterType.WgslType.vec2f,
        )
    }

    include(vertexInputStruct)
    include(vertexOutputStruct)
    include(fragmentOutputStruct)
    include(cameraStruct)
    include(scanlineStruct)

    bindGroup(0, BindingUsage.CAMERA) {
        bind(
            0, "camera", cameraStruct, ShaderBindingType.Uniform,
            hasDynamicOffset = true,
            minBindingSize = (Float.SIZE_BYTES * 16)
                .align(device.limits.minUniformBufferOffsetAlignment)
                .toLong()
        )
        bind(1, "scanline_properties", scanlineStruct, ShaderBindingType.Uniform)
    }

    bindGroup(1, BindingUsage.TEXTURE) {
        bindTexture2d(0, "tex")
        bindSampler(1, "sample")
    }

    bindGroup(2, CrtShader.bindingUsage) {
        bindTexture2d(0, "scanline")
        bindSampler(1, "scanline_sampler")
    }

    vertex {
        main(vertexInputStruct, vertexOutputStruct) {
            """
                var output: VertexOutput;
                output.position = camera.view_proj * vec4f(input.pos.x, input.pos.y, input.pos.z, 1);
                output.color = input.color;
                output.uv = input.uv;
    
                return output;
            """.trimIndent()
        }
    }

    fragment {
        include("CrtFunctions") {
            body {
                """
                    fn blend_overlay(base: vec3f, blend: vec3f) -> vec3f {
                        return mix(1.0 - 2.0 * (1.0 - base) * (1.0 - blend), 2.0 * base * blend, step(base, vec3(0.5)));
                    }
                    
                    fn curve(uv: vec2f) -> vec2f {
                        var out = uv * 2.0 - 1.0;
                        let offset = abs(out.yx) / scanline_properties.curvature;
                        out = out + out * offset * offset;
                        out = out * 0.5 + 0.5;
                        return out;
                    }
                    
                    fn vignette(uv: vec2f) -> f32 {
                        let offset = max(abs(uv.y * 2.0 - 1.0) / 4.0, abs(uv.x * 2.0 - 1.0) / 4.0);
                        return 300 * offset * offset * offset * offset * offset;
                    }
                """.trimIndent()
            }
        }

        main(vertexOutputStruct, fragmentOutputStruct) {
            """
                var uv = curve(input.uv);
                var source_color =  textureSample(tex, sample, uv);
                var scanline_color = mix(vec4(0.5), textureSample(scanline, scanline_sampler, input.uv * scanline_properties.uv_scale), scanline_properties.alpha);
                var output: FragmentOutput;
                output.color = vec4(blend_overlay(source_color.rgb, scanline_color.rgb), source_color.a);
                output.color *= 1.0 - scanline_properties.vignette * vignette(input.uv);
                output.color *= step(0.0, uv.x) * step(uv.x, 1.0) * step(0.0, uv.y) * step(uv.y, 1.0);
                return output;
            """.trimIndent()
        }
    }
}

class CrtShader(
    device: Device,
    val preferredFormat: TextureFormat,
    scanlineSize: Int = 2,
    val scanlineColor: Color = Color.WHITE,
    var vignette: Float = 0.5f,
    var curvature: Vec2f = Vec2f(0.5f, 0.5f),
    var scanlineAlpha: Float = 1f
) : Shader(
    device = device, shaderCode(device)
) {
    var scanlineSize: Int = scanlineSize
        set(value) {
            if (field != value) {
                field = value
                scanlineTexture.release()
                scanlineTexture = createPixmapTexture()
            }
        }

    private var scanlineTexture = createPixmapTexture()
        set(value) {
            field = value
            scanlineTextureBindGroup.release()
            scanlineTextureBindGroup = createBindGroup(bindingUsage, scanlineTexture.view, scanlineTexture.sampler)
                ?: error("Unable to create Scanline bind group!")
        }

    private val scanlinePropertiesFloatBuffer = FloatBuffer(8)

    private val scanlinePropertiesUniformBuffer: GPUBuffer = device.createGPUFloatBuffer(
        "scanlineProperties",
        scanlinePropertiesFloatBuffer.toArray(),
        BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )
    private val scanlinePropertiesUniformBufferBinding: BufferBinding = BufferBinding(scanlinePropertiesUniformBuffer)

    private var scanlineTextureBindGroup = createBindGroup(bindingUsage, scanlineTexture.view, scanlineTexture.sampler)
        ?: error("Unable to create Scanline bind group!")

    private fun createPixmapTexture(): Texture {
        val pixmap = Pixmap(scanlineSize, scanlineSize)
        val gray = Color.fromHex("#FF808080").toAbgr888()
        for (y in 0..pixmap.height) {
            for (x in 0..pixmap.width) {
                pixmap.set(x, y, gray)
            }
        }
        for (x in 0..pixmap.width) {
            pixmap.set(x, 0, scanlineColor.toAbgr888())
        }
        return PixmapTexture(
            device,
            preferredFormat,
            pixmap,
            samplerDescriptor = SamplerDescriptor(addressModeU = AddressMode.REPEAT, addressModeV = AddressMode.REPEAT)
        )
    }

    override fun createBindGroup(usage: BindingUsage, vararg args: IntoBindingResource): BindGroup? {
        val index = bindGroupUsageToGroupIndex[usage] ?: return null
        val layout = layouts[index] ?: return null
        if (usage == BindingUsage.CAMERA) {
            val bindings =
                args.mapIndexed { argsIndex, resource -> BindGroupEntry(argsIndex, resource) }.toMutableList()
                    .apply {
                        add(BindGroupEntry(1, scanlinePropertiesUniformBufferBinding))
                    }
            return device.createBindGroup(
                BindGroupDescriptor(
                    layout,
                    bindings
                )
            )
        }
        return super.createBindGroup(usage, *args)
    }

    override fun setBindGroups(renderPassEncoder: RenderPassEncoder) {
        super.setBindGroups(renderPassEncoder)
        renderPassEncoder.setBindGroup(2, scanlineTextureBindGroup)
    }

    fun updateScanlineProperties(
        screenWidth: Float,
        screenHeight: Float,
    ) {
        scanlinePropertiesFloatBuffer.clear()
        // curvature
        scanlinePropertiesFloatBuffer += 2 + (1 - curvature.x) * 10f
        scanlinePropertiesFloatBuffer += 2 + (1 - curvature.y) * 10f
        // vignette
        scanlinePropertiesFloatBuffer += vignette
        // alpha
        scanlinePropertiesFloatBuffer += scanlineAlpha
        // texel
        scanlinePropertiesFloatBuffer += 1f / screenWidth
        scanlinePropertiesFloatBuffer += 1f / screenHeight
        // uv scale
        scanlinePropertiesFloatBuffer += screenWidth / scanlineTexture.width
        scanlinePropertiesFloatBuffer += screenHeight / scanlineTexture.height
        device.queue.writeBuffer(
            scanlinePropertiesUniformBuffer,
            scanlinePropertiesFloatBuffer
        )
    }

    override fun release() {
        super.release()
        scanlineTexture.release()
    }

    companion object {
        val bindingUsage = BindingUsage("Scanline")
    }
}
