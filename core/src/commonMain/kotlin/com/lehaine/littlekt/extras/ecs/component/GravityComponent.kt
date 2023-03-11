package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
open class GravityComponent : Component<GravityComponent> {
    override fun type(): ComponentType<GravityComponent> = GravityComponent

    open fun calculateDeltaXGravity(): Float = 0f
    open fun calculateDeltaYGravity(): Float = 0f
    open fun calculateDeltaZGravity(): Float = 0f

    companion object : ComponentType<GravityComponent>()
}