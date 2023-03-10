package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.Interval
import com.lehaine.littlekt.extras.GameLevel
import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.LevelCollisionComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class LevelGridMoveSystem(private val level: GameLevel<*>, interval: Interval = Fixed(1 / 30f)) :
    GridMoveSystem(interval) {

    override fun checkXCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent) {
        if (collision is LevelCollisionComponent) {
            collision.level = level
            collision.checkXCollision(grid, move)
        } else {
            super.checkXCollision(grid, move, collision)
        }
    }

    override fun checkYCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent) {
        if (collision is LevelCollisionComponent) {
            collision.level = level
            collision.checkYCollision(grid, move)
        } else {
            super.checkYCollision(grid, move, collision)
        }
    }
}