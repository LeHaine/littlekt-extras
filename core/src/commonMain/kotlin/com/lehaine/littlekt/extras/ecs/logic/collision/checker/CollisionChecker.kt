package com.lehaine.littlekt.extras.ecs.logic.collision.checker

import com.lehaine.littlekt.extras.ecs.component.CollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
open class CollisionChecker {
    open fun preXCheck(grid: GridComponent, move: MoveComponent, collision: CollisionComponent) = Unit
    open fun preYCheck(grid: GridComponent, move: MoveComponent, collision: CollisionComponent) = Unit

    open fun checkXCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent): Int = 0
    open fun checkYCollision(grid: GridComponent, move: MoveComponent, collision: CollisionComponent): Int = 0
}