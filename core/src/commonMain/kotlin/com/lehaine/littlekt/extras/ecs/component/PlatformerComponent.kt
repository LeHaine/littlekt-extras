package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.GroundChecker

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerComponent(val groundChecker: GroundChecker) : Component<PlatformerComponent> {
    var onGround = true

    override fun type() = PlatformerComponent

    companion object : ComponentType<PlatformerComponent>()
}