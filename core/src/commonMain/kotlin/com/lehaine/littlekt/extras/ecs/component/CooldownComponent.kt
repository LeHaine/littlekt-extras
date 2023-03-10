package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.Cooldown

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class CooldownComponent : Component<CooldownComponent> {
    val cd = Cooldown()

    override fun type() = CooldownComponent

    companion object : ComponentType<CooldownComponent>()
}