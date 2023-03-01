package com.lehaine.littlekt.extras.renderable

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class Sprite : Renderable2D() {

    override val renderWidth: Float
        get() = if (overrideWidth) overriddenWidth else slice?.width?.toFloat() ?: 0f

    override val renderHeight: Float
        get() = if (overrideHeight) overriddenHeight else slice?.height?.toFloat() ?: 0f

    var overrideWidth = false
    var overrideHeight = false

    var overriddenWidth = 0f
    var overriddenHeight = 0f

    /**
     * Flips the current rendering of the [Sprite] horizontally.
     */
    var flipX = false

    /**
     * Flips the current rendering of the [Sprite] vertically.
     */
    var flipY = false

    var slice: TextureSlice? = null

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        slice?.let {
            batch.setBlendFunction(blendMode)
            batch.draw(
                it,
                x + localOffsetX,
                y + localOffsetY,
                anchorX * it.originalWidth,
                anchorY * it.originalHeight,
                width = renderWidth,
                height = renderHeight,
                scaleX = scaleX * ppuInv,
                scaleY = scaleY * ppuInv,
                flipX = flipX,
                flipY = flipY,
                rotation = rotation,
                colorBits = color.toFloatBits()
            )
        }
    }
}