package com.lehaine.littlekt.extras.ecs.logic.collision.reactor

import com.lehaine.littlekt.extras.GameLevel
import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class LevelCollisionReactor(private val level: GameLevel<*>) : CollisionReactor() {

    override fun reactXCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: CollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) {
        super.reactXCollision(grid, move, collision, collisionResult)
    }

    override fun reactYCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: CollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) {
        super.reactYCollision(grid, move, collision, collisionResult)
    }
}