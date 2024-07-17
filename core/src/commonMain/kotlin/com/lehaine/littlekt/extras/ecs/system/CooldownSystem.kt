package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.CooldownComponent
import com.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class CooldownSystem : IteratingSystem(family { all(CooldownComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val cooldown = entity[CooldownComponent]
        cooldown.cd.update(deltaTime.seconds)
    }
}