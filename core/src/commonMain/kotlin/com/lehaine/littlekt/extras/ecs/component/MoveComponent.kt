package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
data class MoveComponent(
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var velocityZ: Float = 0f,
    var frictionX: Float = 1f,
    var frictionY: Float = 1f,
    var frictionZ: Float = 1f
) : Component<MoveComponent> {
    override fun type(): ComponentType<MoveComponent> = MoveComponent

    companion object : ComponentType<MoveComponent>()
}