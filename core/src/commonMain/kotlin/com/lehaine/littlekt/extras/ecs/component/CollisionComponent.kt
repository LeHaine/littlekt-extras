package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
open class CollisionComponent : Component<CollisionComponent> {
    open fun checkXCollision(grid: GridComponent, move: MoveComponent): Int = 0
    open fun checkYCollision(grid: GridComponent, move: MoveComponent): Int = 0

    override fun type(): ComponentType<CollisionComponent> = CollisionComponent

    companion object : ComponentType<CollisionComponent>()
}