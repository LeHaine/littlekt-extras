package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.RenderBounds
import com.lehaine.littlekt.extras.ecs.component.Sprite
import com.littlekt.math.Mat3
import com.littlekt.math.MutableVec2f
import com.littlekt.math.Rect
import com.littlekt.math.geom.Angle
import com.littlekt.math.geom.degrees
import com.littlekt.math.geom.normalized
import com.littlekt.math.ife

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class SpriteRenderBoundsCalculationSystem :
    IteratingSystem(family { all(Grid, Sprite, RenderBounds) }) {

    private val _bounds = Rect()
    private val transMat = Mat3()
    private val tempMat = Mat3()

    private val topLeft = MutableVec2f()
    private val topRight = MutableVec2f()
    private val bottomLeft = MutableVec2f()
    private val bottomRight = MutableVec2f()

    override fun onTickEntity(entity: Entity) {
        val grid = entity[Grid]
        val sprite = entity[Sprite]
        val renderBounds = entity[RenderBounds]

        if (grid.dirty || sprite.dirty) {
            grid.dirty = false
            sprite.dirty = false

            val slice = sprite.slice
            val origWidth =
                (if (slice?.rotated == true) slice.actualHeight.toFloat() else slice?.actualWidth?.toFloat())
                    ?: sprite.renderWidth
            val origHeight =
                (if (slice?.rotated == true) slice.actualWidth.toFloat() else slice?.actualHeight?.toFloat())
                    ?: sprite.renderHeight

            calculateBounds(
                grid.x,
                grid.y,
                (origWidth - (slice?.offsetX ?: 0)) * grid.anchorX,
                (origHeight - (slice?.offsetY ?: 0)) * grid.anchorY,
                grid.scaleX,
                grid.scaleY,
                rotation = grid.rotation,
                width = (if (slice?.rotated == true) sprite.renderHeight else sprite.renderWidth) + (slice?.offsetX
                    ?: 0),
                height = (if (slice?.rotated == true) sprite.renderWidth else sprite.renderHeight) + (slice?.offsetY
                    ?: 0)
            )

            renderBounds.bounds.set(_bounds.x, _bounds.y, _bounds.width, _bounds.height)
        }
    }

    private fun calculateBounds(
        x: Float,
        y: Float,
        originX: Float,
        originY: Float,
        scaleX: Float,
        scaleY: Float,
        rotation: Angle,
        width: Float,
        height: Float
    ) {
        if (rotation.normalized.degrees ife 0f) {
            _bounds.let {
                it.x = x - originX * scaleX
                it.y = y - originY * scaleY
                it.width = width * scaleX
                it.height = height * scaleY
            }
        } else {
            transMat.setToTranslate(-x - originX, -y - originY)
            tempMat.setToScale(scaleX, scaleY) // scale
            transMat.mulLeft(tempMat)
            tempMat.setToRotation(rotation) // rotate
            transMat.mulLeft(tempMat)
            tempMat.setToTranslate(x, y) // translate back
            transMat.mulLeft(tempMat)

            // get four corners
            bottomLeft.set(x, y)
            bottomRight.set(x + width, y)
            topLeft.set(x, y + height)
            topRight.set(x + width, y + height)

            bottomLeft.mul(transMat)
            bottomRight.mul(transMat)
            topLeft.mul(transMat)
            topRight.mul(transMat)

            val minX = minOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val maxX = maxOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val minY = minOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)
            val maxY = maxOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)

            _bounds.set(minX, minY, maxX - minX, maxY - minY)
        }
    }
}