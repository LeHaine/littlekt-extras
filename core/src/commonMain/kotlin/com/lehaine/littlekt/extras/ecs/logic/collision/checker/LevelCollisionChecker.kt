package com.lehaine.littlekt.extras.ecs.logic.collision.checker

import com.lehaine.littlekt.extras.GameLevel
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
open class LevelCollisionChecker(protected val level: GameLevel<*>) : CollisionChecker() {
    open var rightCollisionRatio: Float = 0.7f
    open var leftCollisionRatio: Float = 0.3f
    open var bottomCollisionRatio: Float = 0f
    open var topCollisionRatio: Float = 1f
    open var useTopCollisionRatio: Boolean = false

    override fun checkXCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int {
        if (level.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            return 1
        }
        if (level.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            return -1
        }
        return 0
    }

    override fun checkYCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / cellSize)
        if (level.hasCollision(cx, cy + 1) && yr >= heightCoordDiff) {
            return 1
        }
        if (level.hasCollision(cx, cy - 1) && yr <= bottomCollisionRatio) {
            return -1
        }
        return 0
    }

    override fun hasCollision(cx: Int, cy: Int): Boolean = level.hasCollision(cx, cy)
}