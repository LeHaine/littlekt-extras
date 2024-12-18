package com.lehaine.littlekt.extras.ecs.logic.collision.resolver

import com.lehaine.littlekt.extras.ecs.component.GridCollision
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.Move
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.LevelCollisionChecker
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
object LevelCollisionResolver : CollisionResolver() {

    override fun resolveXCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir: Int
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            if (dir == -1) {
                grid.xr = checker.leftCollisionRatio
            }
            if (dir == 1) {
                grid.xr = checker.rightCollisionRatio
            }
        } else {
            super.resolveXCollision(grid, move, collision, dir)
        }
    }

    override fun resolveYCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir: Int
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            val heightCoordDiff =
                if (checker.useTopCollisionRatio) checker.topCollisionRatio else floor(grid.height / grid.gridCellSize)
            if (dir == 1) {
                grid.yr = heightCoordDiff
            }
            if (dir == -1) {
                grid.yr = checker.bottomCollisionRatio
            }
        } else {
            super.resolveYCollision(grid, move, collision, dir)
        }
    }
}