package com.lehaine.littlekt.extras.ecs.logic.collision.checker

import com.lehaine.littlekt.extras.GameLevel
import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.LevelCollisionComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
open class LevelCollisionChecker(private val level: GameLevel<*>) : CollisionChecker() {

    override fun checkXCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent): Int {
        if (collision !is LevelCollisionComponent) return 0

        if (level.hasCollision(grid.cx + 1, grid.cy) && grid.xr >= collision.rightCollisionRatio) {
            grid.xr = collision.rightCollisionRatio
            move.velocityX *= 0.5f
            return 1
        }

        if (level.hasCollision(grid.cx - 1, grid.cy) && grid.xr <= collision.leftCollisionRatio) {
            grid.xr = collision.leftCollisionRatio
            move.velocityX *= 0.5f
            return -1
        }
        return 0
    }

    override fun checkYCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent): Int {
        if (collision !is LevelCollisionComponent) return 0
        val heightCoordDiff =
            if (collision.useTopCollisionRatio) collision.topCollisionRatio else floor(grid.height / grid.gridCellSize)
        if (level.hasCollision(grid.cx, grid.cy - 1) && grid.yr <= heightCoordDiff) {
            grid.yr = heightCoordDiff
            return -1
        }
        if (level.hasCollision(grid.cx, grid.cy + 1) && grid.yr >= collision.bottomCollisionRatio) {
            grid.yr = collision.bottomCollisionRatio
            move.velocityY = 0f
            return 1
        }
        return 0
    }
}