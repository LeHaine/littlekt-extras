package com.lehaine.littlekt.extras

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.extras.entity.Entity
import com.lehaine.littlekt.extras.entity.toPixelPosition
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.OrthographicCamera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.geom.degrees
import com.lehaine.littlekt.util.fastForEach
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 11/8/2022
 */
class CollisionTest(context: Context) : FixedTimeContextListener(context) {

    val dummies = mutableListOf<BitsEntity>()
    val player = PlayerEntity()

    val greenBits = Color.GREEN.toFloatBits()
    val cyanBits = Color.CYAN.toFloatBits()
    val redBits = Color.RED.toFloatBits()

    val input: Input get() = context.input

    open inner class BitsEntity : Entity(8f) {
        var currentBits = 0f

        override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
            super.render(batch, camera, shapeRenderer)
            shapeRenderer.filledRectangle(
                left,
                top,
                width,
                height,
                rotation,
                currentBits
            )
            shapeRenderer.circle(
                centerX,
                centerY,
                outerRadius.toFloat(),
                color = greenBits
            )
        }

        override fun update(dt: Duration) {
            super.update(dt)
            currentBits = if (isCollidingWith(player, true)) redBits else cyanBits
        }
    }

    inner class PlayerEntity : BitsEntity() {
        init {
            width = 256f
            height = 128f
            anchorX = 0f
            anchorY = 0f
        }

        override fun update(dt: Duration) {
            super.update(dt)
            if (input.isKeyPressed(Key.W)) {
                velocityY = -1f
            }
            if (input.isKeyPressed(Key.S)) {
                velocityY = 1f
            }
            if (input.isKeyPressed(Key.A)) {
                velocityX = -1f
            }
            if (input.isKeyPressed(Key.D)) {
                velocityX = 1f
            }
            if (input.isKeyPressed(Key.E)) {
                rotation += 1.degrees
            }
            if (input.isKeyPressed(Key.Q)) {
                rotation -= 1.degrees
            }

            currentBits = if (dummies.any { isCollidingWith(it, true) }) redBits else greenBits
        }
    }

    override suspend fun Context.start() {
        dummies += BitsEntity().apply {
            width = 384f
            height = 256f
            anchorX = 0f
            anchorY = 0f
            currentBits = Color.YELLOW.toFloatBits()
            toPixelPosition(500f, 100f)
        }

        dummies += BitsEntity().apply {
            width = 75f
            height = 93f
            anchorX = 0f
            anchorY = 0f

            currentBits = greenBits
            toPixelPosition(300f, 100f)
        }

        val batch = SpriteBatch(this)
        val shapeRenderer = ShapeRenderer(batch)
        val camera = OrthographicCamera(graphics.width, graphics.height)

        onResize { width, height ->
            camera.ortho(width, height)
        }

        onFixedUpdate {
            player.fixedUpdate()
            dummies.fastForEach { it.fixedUpdate() }
        }

        onRender { dt ->
            gl.clearColor(0.1f, 0.1f, 0.1f, 1f)
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

            player.preUpdate(dt)
            dummies.fastForEach { it.preUpdate(dt) }

            player.fixedProgressionRatio = fixedProgressionRatio
            player.update(dt)
            dummies.fastForEach {
                it.fixedProgressionRatio = fixedProgressionRatio
                it.update(dt)
            }

            player.postUpdate(dt)
            dummies.fastForEach { it.postUpdate(dt) }

            batch.use(camera.viewProjection) {
                player.render(batch, camera, shapeRenderer)
                dummies.fastForEach { it.render(batch, camera, shapeRenderer) }
            }
        }
    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
    }.start {
        CollisionTest(it)
    }
}