package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Particles
import com.littlekt.graphics.g2d.Batch
import com.littlekt.math.Rect
import com.littlekt.util.datastructure.fastForEach

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
open class ParticlesRenderSystem(
    private val batch: Batch,
    private val viewBounds: Rect,
    vararg extraTypes: ComponentType<*> = emptyArray()
) : IteratingSystem(family { all(Particles, *extraTypes) }) {

    override fun onTickEntity(entity: Entity) {
        val particlesComponent = entity[Particles]

        with(particlesComponent) {
            if (particles.isNotEmpty()) {
                batch.setBlendState(blendMode)
            }
            particles.fastForEach {
                if (!it.visible || !it.alive) return@fastForEach

                if (viewBounds.intersects(
                        it.x + x,
                        it.y + y,
                        it.x + x + it.slice.width * it.scaleX * scaleX,
                        it.y + y + it.slice.height * it.scaleY * scaleY
                    )
                ) {
                    batch.draw(
                        it.slice,
                        it.x + x,
                        it.y + y,
                        it.anchorX * it.slice.width,
                        it.anchorY * it.slice.height,
                        scaleX = it.scaleX * scaleX,
                        scaleY = it.scaleY * scaleY,
                        rotation = it.rotation + rotation,
                        color = it.color
                    )
                }
            }

            if (particles.isNotEmpty()) {
                batch.swapToPreviousBlendState()
            }
        }
    }
}