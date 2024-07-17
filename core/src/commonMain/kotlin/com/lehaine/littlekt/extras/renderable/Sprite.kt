package com.lehaine.littlekt.extras.renderable

import com.littlekt.graphics.Camera
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class Sprite : Renderable2D() {

    override val renderBounds: Rect
        get() {
            if (boundsDirty) {
                val origWidth =
                    (if (slice?.rotated == true) slice?.actualHeight?.toFloat() else slice?.actualWidth?.toFloat())
                        ?: renderWidth
                val origHeight =
                    (if (slice?.rotated == true) slice?.actualWidth?.toFloat() else slice?.actualHeight?.toFloat())
                        ?: renderHeight
                calculateBounds(
                    position = position,
                    (origWidth - (slice?.offsetX ?: 0)) * anchorX,
                    (origHeight - (slice?.offsetY ?: 0)) * anchorY,
                    scale = scale,
                    rotation = rotation,
                    width = (if (slice?.rotated == true) renderHeight else renderWidth) + (slice?.offsetX ?: 0),
                    height = (if (slice?.rotated == true) renderWidth else renderHeight) + (slice?.offsetY ?: 0)
                )
                boundsDirty = false
            }
            return _bounds
        }

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
        slice?.let { slice ->
            blendMode?.let {
                batch.setBlendState(it)
            }
            batch.draw(
                slice,
                x + localOffsetX,
                y + localOffsetY,
                anchorX * slice.actualWidth,
                anchorY * slice.actualHeight,
                width = renderWidth,
                height = renderHeight,
                scaleX = scaleX * ppuInv,
                scaleY = scaleY * ppuInv,
                flipX = flipX,
                flipY = flipY,
                rotation = rotation,
                color = color
            )
        }
    }
}