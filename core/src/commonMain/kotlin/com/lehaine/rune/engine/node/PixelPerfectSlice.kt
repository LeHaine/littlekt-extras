package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.FrameBufferNode
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graph.node.render.Material
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.shader.ShaderProgram
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.extras.shader.PixelSmoothFragmentShader
import com.lehaine.littlekt.extras.shader.PixelSmoothVertexShader
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun Node.pixelPerfectSlice(
    callback: @SceneGraphDslMarker PixelPerfectSlice.() -> Unit = {}
): PixelPerfectSlice {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return PixelPerfectSlice().also(callback).addTo(this)
}

/**
 * @author Colton Daily
 * @date 3/23/2022
 */
class PixelPerfectSlice : Node2D() {

    var fbo: FrameBufferNode? = null
        set(value) {
            field?.onFboChanged?.disconnect(this)
            field = value
            value?.onFboChanged?.connect(this) {
                slice = if (value is PixelSmoothFrameBuffer) {
                    TextureSlice(it, 0, (value.height - value.pxHeight), value.pxWidth, value.pxHeight)
                } else {
                    it.slice()
                }
            }
        }

    var scaledDistX = 0f
    var scaledDistY = 0f

    var slice: TextureSlice? = null

    private val smoothCameraShader = ShaderProgram(PixelSmoothVertexShader(), PixelSmoothFragmentShader())

    init {
        material = Material(smoothCameraShader)
    }

    override fun onAddedToScene() {
        super.onAddedToScene()
        smoothCameraShader.prepare(context)
    }

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        slice?.let {
            smoothCameraShader.vertexShader.uTextureSizes.apply(
                smoothCameraShader,
                fbo?.width?.toFloat() ?: 0f,
                fbo?.height?.toFloat() ?: 0f,
                0f,
                0f
            )
            smoothCameraShader.vertexShader.uSampleProperties.apply(
                smoothCameraShader,
                0f,
                0f,
                scaledDistX,
                scaledDistY
            )
            batch.draw(
                it,
                0f,
                0f,
                width = context.graphics.width.toFloat(),
                height = context.graphics.height.toFloat(),
                flipY = true
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        smoothCameraShader.dispose()
    }
}