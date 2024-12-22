package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 12/22/2024
 */
class GridEntityCollision(
    var useSat: Boolean = true,
    override val poolType: PoolType<GridEntityCollision> = GridEntityCollision
) : PoolableComponent<GridEntityCollision> {

    override fun reset() {
        useSat = true
    }

    override fun type(): ComponentType<GridEntityCollision> = GridEntityCollision

    companion object : ComponentType<GridEntityCollision>(), PoolType<GridEntityCollision> {
        override val poolName: String = "gridEntityCollision"
    }
}