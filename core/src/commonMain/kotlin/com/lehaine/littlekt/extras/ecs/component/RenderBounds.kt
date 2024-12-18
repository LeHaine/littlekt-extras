package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class RenderBounds(override val poolType: PoolType<RenderBounds> = RenderBounds) : PoolableComponent<RenderBounds> {
    val bounds = Rect()

    override fun type() = RenderBounds

    override fun reset() {
        bounds.set(0f, 0f, 0f, 0f)
    }

    companion object : ComponentType<RenderBounds>(), PoolType<RenderBounds> {
        override val poolName: String = "renderBoundsPool"
    }
}