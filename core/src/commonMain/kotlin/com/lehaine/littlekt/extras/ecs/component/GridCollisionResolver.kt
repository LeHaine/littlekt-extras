package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.CollisionResolver
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.NoCollisionResolver

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResolver(var resolver: CollisionResolver = NoCollisionResolver) :
    Component<GridCollisionResolver> {
    override fun type() = GridCollisionResolver

    companion object : ComponentType<GridCollisionResolver>()
}