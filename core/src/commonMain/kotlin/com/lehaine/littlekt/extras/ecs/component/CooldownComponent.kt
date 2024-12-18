package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.Cooldown

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class CooldownComponent(override val poolType: PoolType<CooldownComponent> = CooldownComponent) :
    PoolableComponent<CooldownComponent> {
    val cd = Cooldown()

    override fun type() = CooldownComponent

    override fun reset() {
        cd.removeAll()
    }

    companion object : ComponentType<CooldownComponent>(), PoolType<CooldownComponent> {
        override val poolName = "cooldownPool"
    }
}