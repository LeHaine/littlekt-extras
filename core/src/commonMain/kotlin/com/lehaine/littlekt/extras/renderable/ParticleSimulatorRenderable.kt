package com.lehaine.littlekt.extras.renderable

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.ParticleSimulator
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.fastForEach


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
            batch.setBlendFunction(blendMode)
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
                    colorBits = it.color.toFloatBits()
                )
            }
        }
    }

    companion object {
        private val viewBounds: Rect = Rect()
    }
}