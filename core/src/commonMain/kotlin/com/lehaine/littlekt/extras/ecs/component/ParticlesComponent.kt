package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.littlekt.graphics.g2d.Particle
import com.littlekt.graphics.webgpu.BlendState
import com.littlekt.math.geom.Angle

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
    var blendMode: BlendState = BlendState.NonPreMultiplied

    fun add(particle: Particle) {
        particles += particle
    }

    override fun type() = ParticlesComponent

    companion object : ComponentType<ParticlesComponent>()
}