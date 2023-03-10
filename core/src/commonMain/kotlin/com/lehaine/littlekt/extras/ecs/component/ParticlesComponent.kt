package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.graphics.g2d.Particle
import com.lehaine.littlekt.math.geom.Angle

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class ParticlesComponent(val particles: MutableList<Particle> = mutableListOf()) : Component<ParticlesComponent> {
    var x: Float = 0f
    var y: Float = 0f
    var scaleX: Float = 0f
    var scaleY: Float = 0f
    var rotation: Angle = Angle.ZERO

    override fun type() = ParticlesComponent

    companion object : ComponentType<ParticlesComponent>()
}