package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
open class CollisionComponent : Component<CollisionComponent> {

    override fun type(): ComponentType<CollisionComponent> = CollisionComponent

    companion object : ComponentType<CollisionComponent>()
}