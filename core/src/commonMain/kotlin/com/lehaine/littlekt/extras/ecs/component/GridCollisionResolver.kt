package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.CollisionResolver
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.NoCollisionResolver

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResolver(
    var resolver: CollisionResolver = NoCollisionResolver,
    override val poolType: PoolType<GridCollisionResolver> = GridCollisionResolver
) :
    PoolableComponent<GridCollisionResolver> {

    override fun reset() {
        // no-op
    }

    override fun type() = GridCollisionResolver

    companion object : ComponentType<GridCollisionResolver>(), PoolType<GridCollisionResolver> {
        override val poolName: String = "gridCollisionResolverPool"

    }
}