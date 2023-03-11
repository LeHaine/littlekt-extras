package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerComponent : Component<PlatformerComponent> {
    var onGround = true

    override fun type() = PlatformerComponent

    companion object : ComponentType<PlatformerComponent>()
}