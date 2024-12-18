package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.GroundChecker

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class Platformer(val groundChecker: GroundChecker) : Component<Platformer> {
    var onGround = true

    override fun type() = Platformer

    companion object : ComponentType<Platformer>()
}