package com.lehaine.littlekt.extras.ecs.logic.collision.resolver

import com.lehaine.littlekt.extras.ecs.component.GridCollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
abstract class CollisionResolver {

    open fun resolveXCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        dir: Int
    ) = Unit

    open fun resolveYCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        dir: Int
    ) = Unit
}