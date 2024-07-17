package com.lehaine.littlekt.extras.grid.entity

import com.lehaine.littlekt.extras.GameLevel
import com.littlekt.graphics.g2d.tilemap.ldtk.LDtkEntity
import kotlin.math.floor

open class GridLevelEntity(
    protected open val level: GameLevel<*>,
    gridCellSize: Float
) : GridEntity(gridCellSize) {
    open var rightCollisionRatio: Float = 0.7f
    open var leftCollisionRatio: Float = 0.3f
    open var bottomCollisionRatio: Float = 0f
    open var topCollisionRatio: Float = 1f
    open var useTopCollisionRatio: Boolean = false

    fun setFromLevelEntity(data: LDtkEntity) {
        cx = data.cx
        cy = level.height - 1 - data.cy
        xr = data.pivotX
        yr = 1f - data.pivotY
        anchorX = data.pivotX
        anchorY = 1f - data.pivotY
    }

    override fun checkXCollision() {
        if (level.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            xr = rightCollisionRatio
            velocityX *= 0.5f
            onLevelCollision(1, 0)
        }

        if (level.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            xr = leftCollisionRatio
            velocityX *= 0.5f
            onLevelCollision(-1, 0)
        }
    }

    override fun checkYCollision() {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize.toFloat())
        if (level.hasCollision(cx, cy + 1) && yr >= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY = 0f
            onLevelCollision(0, 1)
        }
        if (level.hasCollision(cx, cy - 1) && yr <= bottomCollisionRatio) {
            velocityY = 0f
            yr = bottomCollisionRatio
            onLevelCollision(0, -1)
        }
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) = Unit
}