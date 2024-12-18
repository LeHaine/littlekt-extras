package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Gravity
import com.lehaine.littlekt.extras.ecs.component.Platformer

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerGravitySystem : IteratingSystem(family { all(Platformer, Gravity) }) {

    override fun onTickEntity(entity: Entity) {
        val platformer = entity[Platformer]
        val gravity = entity[Gravity]

        gravity.enableGravityY = !platformer.onGround
    }
}