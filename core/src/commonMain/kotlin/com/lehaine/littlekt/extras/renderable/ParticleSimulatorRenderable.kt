package com.lehaine.littlekt.extras.renderable

import com.littlekt.graphics.Camera
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.ParticleSimulator
import com.littlekt.graphics.g2d.TextureSlice
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.math.Rect
import com.littlekt.util.calculateViewBounds
import com.littlekt.util.datastructure.fastForEach

class ParticleSimulatorRenderable : Renderable2D() {

    var maxParticles = 2048

    override val renderWidth: Float = Float.POSITIVE_INFINITY
    override val renderHeight: Float = Float.POSITIVE_INFINITY

    private val simulator by lazy { ParticleSimulator(maxParticles) }

    fun alloc(slice: TextureSlice, x: Float, y: Float) = simulator.alloc(slice, x, y)

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        viewBounds.calculateViewBounds(camera)
        val blendMode = blendMode
        if (simulator.particles.isNotEmpty() && blendMode != null) {
            batch.setBlendState(blendMode)
        }
        simulator.particles.fastForEach {
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
    }

    companion object {
        private val viewBounds: Rect = Rect()
    }
}