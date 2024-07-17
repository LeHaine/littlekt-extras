package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.littlekt.math.Rect

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class RenderBoundsComponent : Component<RenderBoundsComponent> {
    val bounds = Rect()

    override fun type() = RenderBoundsComponent

    companion object : ComponentType<RenderBoundsComponent>()
}