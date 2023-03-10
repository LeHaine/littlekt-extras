package com.lehaine.littlekt.extras.ecs.logic.collision.reactor

import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
open class CollisionReactor {

    open fun reactXCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: CollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) = Unit

    open fun reactYCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: CollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) = Unit
}