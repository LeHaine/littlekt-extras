package com.lehaine.littlekt.extras.ecs.logic.collision.resolver

import com.lehaine.littlekt.extras.ecs.component.GridCollision
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.Move
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.CollisionChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.ObliqueCollisionChecker
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class ObliqueCollisionResolver : CollisionResolver() {

    var wallSlideDelta: Float = 0.005f
    var wallSlideTolerance: Float = 0.015f
    var wallDeltaRightCollisionRatio: Float = 0.5f
    var wallDeltaLeftCollisionRatio: Float = 0.5f
    var wallDeltaBottomCollisionRatio: Float = 0.6f
    var wallDeltaTopCollisionRatio: Float = 0.6f

    override fun resolveXCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir:Int
    ) {
        val checker = collision.checker
        if (checker is ObliqueCollisionChecker) {
            // check collision left side
            if (dir == -1) {
                grid.xr = checker.leftCollisionRatio
                move.velocityX *= 0.5f

                // check if player is stuck on wall / corner and help them by nudging them off it
                if (shouldNudge(
                        grid.yr,
                        checker,
                        wallDeltaTopCollisionRatio,
                        grid.cx,
                        grid.cy,
                        -1,
                        -1,
                        move.velocityY,
                        true
                    )
                ) {
                    move.velocityY -= wallSlideDelta
                }
                if (shouldNudge(
                        grid.yr,
                        checker,
                        wallDeltaBottomCollisionRatio,
                        grid.cx,
                        grid.cy,
                        -1,
                        1,
                        move.velocityY,
                        false
                    )
                ) {
                    move.velocityY += wallSlideDelta
                }
            }

            // check collision right side
            if (dir == 1) {
                grid.xr = checker.rightCollisionRatio
                move.velocityX *= 0.5f

                // check if player is stuck on wall / corner and help them by nudging them off it
                if (shouldNudge(
                        grid.yr,
                        checker,
                        wallDeltaTopCollisionRatio,
                        grid.cx,
                        grid.cy,
                        1,
                        -1,
                        move.velocityY,
                        true
                    )
                ) {
                    move.velocityY -= wallSlideDelta
                }
                if (shouldNudge(
                        grid.yr,
                        checker,
                        wallDeltaBottomCollisionRatio,
                        grid.cx,
                        grid.cy,
                        1,
                        1,
                        move.velocityY,
                        false
                    )
                ) {
                    move.velocityY += wallSlideDelta
                }

            }
        } else {
            super.resolveXCollision(grid, move, collision, dir)
        }
    }

    override fun resolveYCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir:Int
    ) {
        val checker = collision.checker
        if (checker is ObliqueCollisionChecker) {
            val heightCoordDiff =
                if (checker.useTopCollisionRatio) checker.topCollisionRatio else floor(grid.height / grid.gridCellSize)
            // check top collision
            if (dir == -1) {
                grid.yr = heightCoordDiff
                move.velocityY = 0f
                // check if player is stuck on wall / corner and help them by nudging them off it
                if (shouldNudge(
                        gridRatio = grid.xr,
                        checker = checker,
                        collisionRatio = wallDeltaLeftCollisionRatio,
                        cx = grid.cx,
                        cy = grid.cy,
                        xDir = -1,
                        yDir = 1,
                        velocity = move.velocityX,
                        lowerThanTolerance = true
                    )
                ) {
                    move.velocityX -= wallSlideDelta // todo fix this calculation
                }
                if (shouldNudge(
                        gridRatio = grid.xr,
                        checker = checker,
                        collisionRatio = wallDeltaRightCollisionRatio,
                        cx = grid.cx,
                        cy = grid.cy,
                        xDir = 1,
                        yDir = 1,
                        velocity = move.velocityX,
                        lowerThanTolerance = false
                    )
                ) {
                    move.velocityX += wallSlideDelta
                }
            }

            // check bottom collision
            if (dir == 1) {
                grid.yr = checker.bottomCollisionRatio
                move.velocityY = 0f

                // check if player is stuck on wall / corner and help them by nudging them off it
                if (shouldNudge(
                        gridRatio = grid.xr,
                        checker = checker,
                        collisionRatio = wallDeltaLeftCollisionRatio,
                        cx = grid.cx,
                        cy = grid.cy,
                        xDir = -1,
                        yDir = -1,
                        velocity = move.velocityX,
                        lowerThanTolerance = true
                    )
                ) {
                    move.velocityX -= wallSlideDelta
                }
                if (shouldNudge(
                        gridRatio = grid.xr,
                        checker = checker,
                        collisionRatio = wallDeltaRightCollisionRatio,
                        cx = grid.cx,
                        cy = grid.cy,
                        xDir = 1,
                        yDir = -1,
                        velocity = move.velocityX,
                        lowerThanTolerance = false
                    )
                ) {
                    move.velocityX += wallSlideDelta
                }
            }
        } else {
            super.resolveYCollision(grid, move, collision, dir)
        }
    }

    private fun shouldNudge(
        gridRatio: Float,
        checker: CollisionChecker,
        collisionRatio: Float,
        cx: Int,
        cy: Int,
        xDir: Int,
        yDir: Int,
        velocity: Float,
        lowerThanTolerance: Boolean
    ): Boolean {
        return gridRatio < collisionRatio && !checker.hasCollision(
            cx + xDir,
            cy + yDir
        ) && ((lowerThanTolerance && velocity <= wallSlideTolerance) || (!lowerThanTolerance && velocity >= wallSlideTolerance))
    }
}