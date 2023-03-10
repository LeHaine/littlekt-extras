package com.lehaine.littlekt.extras.ecs.component

import com.lehaine.littlekt.extras.GameLevel
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
open class LevelCollisionComponent : CollisionComponent() {
    open var rightCollisionRatio: Float = 0.7f
    open var leftCollisionRatio: Float = 0.3f
    open var bottomCollisionRatio: Float = 1f
    open var topCollisionRatio: Float = 1f
    open var useTopCollisionRatio: Boolean = false

//    override fun checkXCollision(grid: GridComponent, move: MoveComponent): Int {
//        if (level.hasCollision(grid.cx + 1, grid.cy) && grid.xr >= rightCollisionRatio) {
//            grid.xr = rightCollisionRatio
//            move.velocityX *= 0.5f
//            return 1
//        }
//
//        if (level.hasCollision(grid.cx - 1, grid.cy) && grid.xr <= leftCollisionRatio) {
//            grid.xr = leftCollisionRatio
//            move.velocityX *= 0.5f
//            return -1
//        }
//        return 0
//    }
//
//    override fun checkYCollision(grid: GridComponent, move: MoveComponent): Int {
//        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(grid.height / grid.gridCellSize)
//        if (level.hasCollision(grid.cx, grid.cy - 1) && grid.yr <= heightCoordDiff) {
//            grid.yr = heightCoordDiff
//            return -1
//        }
//        if (level.hasCollision(grid.cx, grid.cy + 1) && grid.yr >= bottomCollisionRatio) {
//            grid.yr = bottomCollisionRatio
//            move.velocityY = 0f
//            return 1
//        }
//        return 0
//    }
}