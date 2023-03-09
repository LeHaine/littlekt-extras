package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.RenderBoundsComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent
import com.lehaine.littlekt.math.Mat3
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.normalized

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class RenderBoundsCalculationSystem :
    IteratingSystem(family { all(GridComponent, SpriteComponent, RenderBoundsComponent) }) {

    private val _bounds = Rect()
    private val transMat = Mat3()
    private val tempMat = Mat3()

    private val topLeft = MutableVec2f()
    private val topRight = MutableVec2f()
    private val bottomLeft = MutableVec2f()
    private val bottomRight = MutableVec2f()

    override fun onTickEntity(entity: Entity) {
        val grid = entity[GridComponent]
        val sprite = entity[SpriteComponent]
        val renderBounds = entity[RenderBoundsComponent]
        val slice = sprite.slice

        val origWidth =
            (if (slice?.rotated == true) slice.originalHeight.toFloat() else slice?.originalWidth?.toFloat())
                ?: sprite.renderWidth
        val origHeight =
            (if (slice?.rotated == true) slice.originalWidth.toFloat() else slice?.originalHeight?.toFloat())
                ?: sprite.renderHeight

        calculateBounds(
            grid.x, grid.y, grid.anchorX, grid.anchorY,
            (origWidth - (slice?.offsetX ?: 0)) * grid.anchorX,
            (origHeight - (slice?.offsetY ?: 0)) * grid.anchorY,
            grid.scaleX,
            grid.scaleY,
            rotation = grid.rotation,
            width = if (slice?.rotated == true) sprite.renderHeight else sprite.renderWidth,
            height = if (slice?.rotated == true) sprite.renderWidth else sprite.renderHeight
        )

        renderBounds.bounds.set(_bounds.x, _bounds.y, _bounds.width, _bounds.height)
    }

    private fun calculateBounds(
        x: Float,
        y: Float,
        anchorX: Float,
        anchorY: Float,
        originX: Float,
        originY: Float,
        scaleX: Float,
        scaleY: Float,
        rotation: Angle,
        width: Float,
        height: Float
    ) {
        if (rotation.normalized == Angle.ZERO) {
            _bounds.let {
                it.x = x - originX * scaleX + (width * (1f - anchorX) * scaleX)
                it.y = y - originY * scaleY + (height * (1f - anchorY) * scaleY)
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