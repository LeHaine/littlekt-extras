package com.lehaine.littlekt.extras.shader

import com.littlekt.file.FloatBuffer
import com.littlekt.graphics.Texture
import com.littlekt.graphics.shader.SpriteShader
import com.littlekt.graphics.webgpu.*
import com.littlekt.util.align

//
//import com.littlekt.graphics.shader.FragmentShaderModel
//import com.littlekt.graphics.shader.ShaderParameter
//import com.littlekt.graphics.shader.VertexShaderModel
//

// lang=wgsl
private val pixelSmoothVertexShaderWgslSrc: String =
    """
    struct CameraUniform {
        view_proj: mat4x4<f32>
    };
    
    struct TextureSize {
        size: vec4<f32>
    };
    
    struct SampleProperties {
        properties: vec4<f32>
    };
    
    @group(0) @binding(0)
    var<uniform> camera: CameraUniform;
    @group(0) @binding(1)
    var<uniform> textureSize: TextureSize;
    @group(0) @binding(2)
    var<uniform> sampleProperties: SampleProperties;
    
    struct VertexOutput {
        @location(0) color: vec4<f32>,
        @location(1) uv: vec2<f32>,
        @builtin(position) position: vec4<f32>,
    };
    
    @vertex
    fn vs_main(
        @location(0) pos: vec3<f32>,
        @location(1) color: vec4<f32>,
        @location(2) uvs: vec2<f32>) -> VertexOutput {

        var output: VertexOutput;
        output.position = camera.view_proj * vec4<f32>(pos.x, pos.y, pos.z, 1);
        output.color = color;
        output.uv.x = uvs.x + sampleProperties.properties.z / textureSize.size.x;
        output.uv.y = uvs.y - sampleProperties.properties.y / textureSize.size.y; 

        return output;
    }

    @group(1) @binding(0)
    var tex: texture_2d<f32>;
    @group(1) @binding(1)
    var sample: sampler;

    @fragment
    fn fs_main(in: VertexOutput) -> @location(0) vec4<f32> {
        let dU: f32 = 1.0 / textureSize.size.x;
        let dV: f32 = 1.0 / textureSize.size.y;
        
        let c0: vec4<f32> = textureSample(tex, sample, in.uv);
        let c1: vec4<f32> = textureSample(tex, sample, in.uv + vec2<f32>(dU, 0));
        let c2: vec4<f32> = textureSample(tex, sample, in.uv + vec2<f32>(0, dV));
        let c3: vec4<f32> = textureSample(tex, sample, in.uv + vec2<f32>(dU, dV));
        
        let subU: f32 = sampleProperties.properties.x;
        let subV: f32 = sampleProperties.properties.y;
        
        let w0: f32 = 1 - subU;
        let w1: f32 = subU;
        let w2: f32 = 1 - subV;
        let w3: f32 = subV;
        
        let bilinear: vec4<f32> = c0 * w0 * w2 + c1 * w1 * w2 + c2 * w0 * w3 + c3 * w1 * w3;
        return bilinear * in.color;
    }
""".trimIndent()

class PixelSmoothCameraSpriteShader(device: Device, cameraDynamicSize: Int = 50) : SpriteShader(
    device = device, src = pixelSmoothVertexShaderWgslSrc,
    layout =
    listOf(
        BindGroupLayoutDescriptor(
            listOf(
                // camera
                BindGroupLayoutEntry(
                    0,
                    ShaderStage.VERTEX,
                    BufferBindingLayout(
                        type = BufferBindingType.UNIFORM,
                        hasDynamicOffset = true,
                        minBindingSize =
                        (Float.SIZE_BYTES * 16)
                            .align(device.limits.minUniformBufferOffsetAlignment)
                            .toLong()
                    )
                ),
                // texture size
                BindGroupLayoutEntry(
                    1,
                    ShaderStage.VERTEX or ShaderStage.FRAGMENT,
                    BufferBindingLayout(type = BufferBindingType.UNIFORM)
                ),

                // sample properties
                BindGroupLayoutEntry(
                    2,
                    ShaderStage.VERTEX or ShaderStage.FRAGMENT,
                    BufferBindingLayout(type = BufferBindingType.UNIFORM)
                ),
            )
        ),
        BindGroupLayoutDescriptor(
            listOf(
                BindGroupLayoutEntry(0, ShaderStage.FRAGMENT, TextureBindingLayout()),
                BindGroupLayoutEntry(1, ShaderStage.FRAGMENT, SamplerBindingLayout())
            )
        )
    ),
    cameraDynamicSize = cameraDynamicSize
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

    override fun MutableList<BindGroup>.createBindGroupsWithTexture(texture: Texture, data: Map<String, Any>) {
        add(
            device.createBindGroup(
                BindGroupDescriptor(
                    layouts[0],
                    listOf(
                        BindGroupEntry(0, cameraUniformBufferBinding),
                        BindGroupEntry(1, textureSizesUniformBufferBinding),
                        BindGroupEntry(2, samplePropertiesUniformBufferBinding)
                    )
                )
            )
        )
        add(
            device.createBindGroup(
                BindGroupDescriptor(
                    layouts[1],
                    listOf(BindGroupEntry(0, texture.view), BindGroupEntry(1, texture.sampler))
                )
            )
        )
    }

    override fun setBindGroups(encoder: RenderPassEncoder, bindGroups: List<BindGroup>, dynamicOffsets: List<Long>) {
        encoder.setBindGroup(0, bindGroups[0], dynamicOffsets)
        encoder.setBindGroup(1, bindGroups[1])
    }

    fun updateTextureSize(x: Float, y: Float) {
        textureSizesFloatBuffer.clear()
        textureSizesFloatBuffer += x
        textureSizesFloatBuffer += y
        textureSizesFloatBuffer += 0f
        textureSizesFloatBuffer += 0f
        device.queue.writeBuffer(
            textureSizesUniformBuffer,
            textureSizesFloatBuffer
        )
    }

    fun updateSampleProperties(x: Float, y: Float) {
        samplePropertiesFloatBuffer.clear()
        samplePropertiesFloatBuffer += 0f
        samplePropertiesFloatBuffer += 0f
        samplePropertiesFloatBuffer += x
        samplePropertiesFloatBuffer += y
        device.queue.writeBuffer(
            samplePropertiesUniformBuffer,
            samplePropertiesFloatBuffer
        )
    }
}
