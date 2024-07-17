package com.lehaine.littlekt.extras.renderable

import com.littlekt.graphics.Camera
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.Particle
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.toFloatBits
import com.littlekt.math.Rect
import com.littlekt.util.calculateViewBounds
import com.littlekt.util.datastructure.fastForEach
import kotlin.time.Duration


class ParticleBatch : Renderable2D() {

    override val renderWidth: Float = Float.POSITIVE_INFINITY
    override val renderHeight: Float = Float.POSITIVE_INFINITY

    private val particles = mutableListOf<Particle>()

    fun add(particle: Particle) {
        particles += particle
    }

    fun update(dt: Duration) {
        particles.fastForEach {
            if (it.killed || !it.alive) {
                particles -= it
            }
        }
    }


    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        viewBounds.calculateViewBounds(camera)
        val blendMode = blendMode
        if (particles.isNotEmpty() && blendMode != null) {
            batch.setBlendState(blendMode)
        }
        particles.fastForEach {
            if (!it.visible || !it.alive) return@fastForEach

            if (viewBounds.intersects(
                    it.x + x,
                    it.y + y,
                    it.x + x + it.slice.width * it.scaleX * scaleX * ppuInv,
                    it.y + y + it.slice.height * it.scaleY * scaleY * ppuInv
                )
            ) {
                batch.draw(
                    it.slice,
                    it.x + x,
                    it.y + y,
                    it.anchorX * it.slice.width,
                    it.anchorY * it.slice.height,
                    scaleX = it.scaleX * scaleX * ppuInv,
                    scaleY = it.scaleY * scaleY * ppuInv,
                    rotation = it.rotation + rotation,
                    color = it.color
                )
            }
        }
        if (blendMode != null) batch.swapToPreviousBlendState()
    }

    companion object {
        private val viewBounds: Rect = Rect()
    }
}