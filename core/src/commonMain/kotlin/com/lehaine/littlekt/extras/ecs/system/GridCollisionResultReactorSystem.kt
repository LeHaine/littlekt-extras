package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.*
import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.logic.collision.reactor.CollisionReactor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResultReactorSystem(
    private val collisionReactor: CollisionReactor,
    interval: Interval = Fixed(1 / 30f)
) : IteratingSystem(family = World.family {
    all(GridComponent, MoveComponent, CollisionComponent).any(
        GridCollisionResultComponent.GridCollisionX,
        GridCollisionResultComponent.GridCollisionY
    )
}, interval = interval) {

    override fun onTickEntity(entity: Entity) {
        val grid = entity[GridComponent]
        val move = entity[MoveComponent]
        val collision = entity[CollisionComponent]

        entity.getOrNull(GridCollisionResultComponent.GridCollisionX)?.let {
            collisionReactor.reactXCollision(grid, move, collision, it)
        }
        entity.getOrNull(GridCollisionResultComponent.GridCollisionY)?.let {
            collisionReactor.reactYCollision(grid, move, collision, it)
        }
    }
}