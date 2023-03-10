package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.*
import com.lehaine.littlekt.extras.ecs.component.*

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResolverSystem(
    interval: Interval = Fixed(1 / 30f)
) : IteratingSystem(family = World.family {
    all(GridComponent, MoveComponent, GridCollisionComponent, GridCollisionResolverComponent).any(
        GridCollisionResultComponent.GridCollisionX,
        GridCollisionResultComponent.GridCollisionY
    )
}, interval = interval) {

    override fun onTickEntity(entity: Entity) {
        val grid = entity[GridComponent]
        val move = entity[MoveComponent]
        val collision = entity[GridCollisionComponent]
        val resolver = entity[GridCollisionResolverComponent]

        entity.getOrNull(GridCollisionResultComponent.GridCollisionX)?.let {
            resolver.resolver.resolveXCollision(grid, move, collision, it)
        }
        entity.getOrNull(GridCollisionResultComponent.GridCollisionY)?.let {
            resolver.resolver.resolveYCollision(grid, move, collision, it)
        }
    }
}