package com.lehaine.littlekt.extras.ecs.logic.collision.resolver

import com.lehaine.littlekt.extras.ecs.component.GridCollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.LevelCollisionChecker
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
object LevelCollisionResolver : CollisionResolver() {

    override fun resolveXCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        dir: Int
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            if (dir == -1) {
                grid.xr = checker.leftCollisionRatio
                move.velocityX *= 0.5f
            }
            if (dir == 1) {
                grid.xr = checker.rightCollisionRatio
                move.velocityX *= 0.5f
            }
        } else {
            super.resolveXCollision(grid, move, collision, dir)
        }
    }

    override fun resolveYCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        dir: Int
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            val heightCoordDiff =
                if (checker.useTopCollisionRatio) checker.topCollisionRatio else floor(grid.height / grid.gridCellSize)
            if (dir == -1) {
                grid.yr = heightCoordDiff
                move.velocityY = 0f
            }
            if (dir == 1) {
                grid.yr = checker.bottomCollisionRatio
                move.velocityY = 0f
            }
        } else {
            super.resolveYCollision(grid, move, collision, dir)
        }
    }
}