package com.lehaine.littlekt.extras.ecs.logic.collision.resolver

import com.lehaine.littlekt.extras.ecs.component.GridCollision
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.Move

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
abstract class CollisionResolver {

    open fun resolveXCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir: Int
    ) = Unit

    open fun resolveYCollision(
        grid: Grid,
        move: Move,
        collision: GridCollision,
        dir: Int
    ) = Unit
}