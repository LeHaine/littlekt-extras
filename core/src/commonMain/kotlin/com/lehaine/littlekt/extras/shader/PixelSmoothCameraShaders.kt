package com.lehaine.littlekt.extras.shader

import com.littlekt.file.FloatBuffer
import com.littlekt.graphics.shader.Shader
import com.littlekt.graphics.shader.builder.ShaderBindingType
import com.littlekt.graphics.shader.builder.ShaderStructParameterType
import com.littlekt.graphics.shader.builder.shader
import com.littlekt.graphics.shader.builder.shaderStruct
import com.littlekt.graphics.util.BindingUsage
import com.littlekt.graphics.webgpu.*
import com.littlekt.util.align

private fun pixelSmoothShaderCode(device: Device) = shader {
    val vertexInputStruct = shaderStruct("VertexInput") {
        mapOf(
            "pos" to ShaderStructParameterType.Location(0, ShaderStructParameterType.WgslType.vec3f),
            "color" to ShaderStructParameterType.Location(1, ShaderStructParameterType.WgslType.vec4f),
            "uvs" to ShaderStructParameterType.Location(2, ShaderStructParameterType.WgslType.vec2f),
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

    val textureSizeStruct = shaderStruct("TextureSize") {
        mapOf("size" to ShaderStructParameterType.WgslType.vec4f)
    }

    val samplePropertiesStruct = shaderStruct("SampleProperties") {
        mapOf("properties" to ShaderStructParameterType.WgslType.vec4f)
    }

    include(vertexInputStruct)
    include(vertexOutputStruct)
    include(fragmentOutputStruct)
    include(cameraStruct)
    include(textureSizeStruct)
    include(samplePropertiesStruct)

    bindGroup(0, BindingUsage.CAMERA) {
        bind(
            0, "camera", cameraStruct, ShaderBindingType.Uniform,
            hasDynamicOffset = true,
            minBindingSize = (Float.SIZE_BYTES * 16)
                .align(device.limits.minUniformBufferOffsetAlignment)
                .toLong()
        )
        bind(1, "textureSize", textureSizeStruct, ShaderBindingType.Uniform)
        bind(2, "sampleProperties", samplePropertiesStruct, ShaderBindingType.Uniform)
    }

    bindGroup(1, BindingUsage.TEXTURE) {
        bindTexture2d(0, "tex")
        bindSampler(1, "sample")
    }

    vertex {
        main(vertexInputStruct, vertexOutputStruct) {
            """
                var output: VertexOutput;
                output.position = camera.view_proj * vec4f(input.pos.x, input.pos.y, input.pos.z, 1);
                output.color = input.color;
                let upscale = textureSize.size.z;
                output.uv.x = input.uvs.x + (sampleProperties.properties.z / upscale) / textureSize.size.x;
                output.uv.y = input.uvs.y - (sampleProperties.properties.w / upscale) / textureSize.size.y; 
    
                return output;
            """.trimIndent()
        }
    }

    fragment {
        main(vertexOutputStruct, fragmentOutputStruct) {
            """
                let upscale: f32 = textureSize.size.z;
                let dU: f32 = (1.0 / upscale) / textureSize.size.x;
                let dV: f32 = (1.0 / upscale) /  textureSize.size.y;
                
                let c0: vec4f = textureSample(tex, sample, input.uv);
                let c1: vec4f = textureSample(tex, sample, input.uv + vec2f(dU, 0));
                let c2: vec4f = textureSample(tex, sample, input.uv + vec2f(0, dV));
                let c3: vec4f = textureSample(tex, sample, input.uv + vec2f(dU, dV));
                
                let subU: f32 = sampleProperties.properties.x;
                let subV: f32 = sampleProperties.properties.y;
                
                let w0: f32 = 1.0 - subU;
                let w1: f32 = subU;
                let w2: f32 = 1.0 - subV;
                let w3: f32 = subV;
                
                let bilinear: vec4f = c0 * w0 * w2 + c1 * w1 * w2 + c2 * w0 * w3 + c3 * w1 * w3;
                var output: FragmentOutput;
                output.color = vec4(bilinear.xyz * input.color.rgb, input.color.a);
                return output;
            """.trimIndent()
        }
    }
}

class PixelSmoothCameraSpriteShader(device: Device) : Shader(
    device = device, pixelSmoothShaderCode(device)
) {

    private val textureSizesFloatBuffer = FloatBuffer(4)
    private val samplePropertiesFloatBuffer = FloatBuffer(4)

    private val textureSizesUniformBuffer: GPUBuffer = device.createGPUFloatBuffer(
        "textureSizes",
        textureSizesFloatBuffer.toArray(),
        BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )
    private val samplePropertiesUniformBuffer: GPUBuffer = device.createGPUFloatBuffer(
        "sampleProperties",
        samplePropertiesFloatBuffer.toArray(),
        BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )

    private val textureSizesUniformBufferBinding: BufferBinding = BufferBinding(textureSizesUniformBuffer)
    private val samplePropertiesUniformBufferBinding: BufferBinding = BufferBinding(samplePropertiesUniformBuffer)

    override fun createBindGroup(usage: BindingUsage, vararg args: IntoBindingResource): BindGroup? {
        val index = bindGroupUsageToGroupIndex[usage] ?: return null
        val layout = layouts[index] ?: return null
        if (usage == BindingUsage.CAMERA) {
            val bindings =
                args.mapIndexed { argsIndex, resource -> BindGroupEntry(argsIndex, resource) }.toMutableList()
                    .apply {
                        add(BindGroupEntry(1, textureSizesUniformBufferBinding))
                        add(BindGroupEntry(2, samplePropertiesUniformBufferBinding))
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

    fun updateTextureSize(x: Float, y: Float, upscale:Float) {
        textureSizesFloatBuffer.clear()
        textureSizesFloatBuffer += x
        textureSizesFloatBuffer += y
        textureSizesFloatBuffer += upscale
        textureSizesFloatBuffer += 0f
        device.queue.writeBuffer(
            textureSizesUniformBuffer,
            textureSizesFloatBuffer
        )
    }

    fun updateSampleProperties(subpixelX: Float, subpixelY: Float, upscaleX: Float, upscaleY: Float) {
        samplePropertiesFloatBuffer.clear()
        samplePropertiesFloatBuffer += subpixelX
        samplePropertiesFloatBuffer += subpixelY
        samplePropertiesFloatBuffer += upscaleX
        samplePropertiesFloatBuffer += upscaleY
        device.queue.writeBuffer(
            samplePropertiesUniformBuffer,
            samplePropertiesFloatBuffer
        )
    }
}
