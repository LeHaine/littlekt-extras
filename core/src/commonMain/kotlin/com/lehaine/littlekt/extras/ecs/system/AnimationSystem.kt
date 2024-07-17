package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.AnimationComponent
import com.lehaine.littlekt.extras.ecs.component.SpriteComponent
import com.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class AnimationSystem : IteratingSystem(family { all(SpriteComponent, AnimationComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val sprite = entity[SpriteComponent]
        val animation = entity[AnimationComponent]

        animation.update(deltaTime.seconds)
        sprite.slice = animation.currentAnimation?.getFrame(animation.currentFrameIdx) ?: sprite.slice
    }
}