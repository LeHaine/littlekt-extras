package com.lehaine.littlekt.extras.shader

import com.lehaine.littlekt.graphics.shader.FragmentShaderModel
import com.lehaine.littlekt.graphics.shader.ShaderParameter
import com.lehaine.littlekt.graphics.shader.VertexShaderModel

class PixelSmoothVertexTextureArrayShader : VertexShaderModel() {
    val uProjTrans = ShaderParameter.UniformMat4("u_projTrans")
    val uTextureSizes = ShaderParameter.UniformVec4("u_textureSizes")
    val uSampleProperties = ShaderParameter.UniformVec4("u_sampleProperties")
    val aPosition = ShaderParameter.Attribute("a_position")
    val aColor = ShaderParameter.Attribute("a_color")
    val aTexCoord0 = ShaderParameter.Attribute("a_texCoord0")
    val aTextureIndex = ShaderParameter.Attribute("a_textureIndex")

    override val parameters: LinkedHashSet<ShaderParameter> =
        linkedSetOf(uProjTrans, uTextureSizes, uSampleProperties, aPosition, aColor, aTexCoord0, aTextureIndex)

    // language=GLSL
    override var source: String = """
        uniform mat4 u_projTrans;
        uniform vec4 u_textureSizes;
        uniform vec4 u_sampleProperties;
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        attribute float a_textureIndex;
        varying lowp vec4 v_color;
        varying vec2 v_texCoords;
        varying float v_textureIndex;

        void main()
        {
            v_color = a_color;
            v_color.a = v_color.a * (255.0/254.0);

            vec2 uvSize = u_textureSizes.xy;

            v_texCoords.x = a_texCoord0.x + u_sampleProperties.z / uvSize.x;
            v_texCoords.y = a_texCoord0.y - u_sampleProperties.w / uvSize.y;
            v_textureIndex = a_textureIndex;

            gl_Position = u_projTrans * a_position;
        }
    """.trimIndent()
}


class PixelSmoothFragmentTextureArrayShader : FragmentShaderModel() {
    val uTextureArray = ShaderParameter.UniformSample2D("u_textureArray")
    val uTextureSizes = ShaderParameter.UniformVec4("u_textureSizes")
    val uSampleProperties = ShaderParameter.UniformVec4("u_sampleProperties")

    override val parameters: LinkedHashSet<ShaderParameter> = linkedSetOf(uTextureArray, uTextureSizes, uSampleProperties)

    // language=GLSL
    override var source: String = """
        uniform sampler2DArray u_textureArray;
        uniform vec4 u_textureSizes;
        uniform vec4 u_sampleProperties;
        varying float v_textureIndex;
        varying lowp vec4 v_color;
        varying vec2 v_texCoords;

        void main()
        {
            vec2 uv = v_texCoords;
            vec2 uvSize = u_textureSizes.xy;

            float dU = 1.0 / uvSize.x;
            float dV = 1.0 / uvSize.y;

            vec4 c0 = texture(u_textureArray, vec3(uv, v_textureIndex));
            vec4 c1 = texture(u_textureArray, vec3(uv + vec2(dU, 0), v_textureIndex));
            vec4 c2 = texture(u_textureArray, vec3(uv + vec2(0, dV), v_textureIndex));
            vec4 c3 = texture(u_textureArray, vec3(uv + vec2(dU, dV), v_textureIndex));

            float subU = u_sampleProperties.x;
            float subV = u_sampleProperties.y;

            float w0 = 1.0 - subU;
            float w1 = subU;
            float w2 = 1.0 - subV;
            float w3 = subV;

            vec4 bilinear = c0 * w0 * w2 + c1 * w1 * w2 + c2 * w0 * w3 + c3 * w1 * w3;

            gl_FragColor = bilinear;
        }
    """.trimIndent()
}