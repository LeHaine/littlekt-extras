package com.lehaine.littlekt.extras

import com.lehaine.littlekt.extras.grid.entity.GridEntity
import com.lehaine.littlekt.extras.grid.entity.toPixelPosition
import com.littlekt.Context
import com.littlekt.createLittleKtApp
import com.littlekt.graphics.Camera
import com.littlekt.graphics.Color
import com.littlekt.graphics.OrthographicCamera
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.SpriteBatch
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.g2d.use
import com.littlekt.graphics.webgpu.*
import com.littlekt.input.Input
import com.littlekt.input.Key
import com.littlekt.math.geom.degrees
import com.littlekt.util.datastructure.fastForEach
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 11/8/2022
 */
class CollisionTest(context: Context) : FixedTimeContextListener(context) {

    val dummies = mutableListOf<BitsEntity>()
    val player = PlayerEntity()
    val input: Input get() = context.input

    open inner class BitsEntity : GridEntity(8f) {
        var color = Color.WHITE

        override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
            super.render(batch, camera, shapeRenderer)
            shapeRenderer.filledRectangle(
                left,
                bottom,
                width,
                height,
                rotation,
                color
            )
            shapeRenderer.circle(
                centerX,
                centerY,
                innerRadius,
                color = Color.YELLOW
            )
            shapeRenderer.circle(
                centerX,
                centerY,
                encompassingRadius,
                color = Color.GREEN
            )
        }

        override fun update(dt: Duration) {
            super.update(dt)
            color = if (isCollidingWith(player, true)) Color.RED else Color.CYAN
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
                velocityY = 1f
            }
            if (input.isKeyPressed(Key.S)) {
                velocityY = -1f
            }
            if (input.isKeyPressed(Key.A)) {
                velocityX = -1f
            }
            if (input.isKeyPressed(Key.D)) {
                velocityX = 1f
            }
            if (input.isKeyPressed(Key.E)) {
                rotation -= 1.degrees
            }
            if (input.isKeyPressed(Key.Q)) {
                rotation += 1.degrees
            }

            color = if (dummies.any { isCollidingWith(it, true) }) Color.RED else Color.GREEN
        }
    }

    override suspend fun Context.start() {
        dummies += BitsEntity().apply {
            width = 384f
            height = 256f
            anchorX = 0f
            anchorY = 0f
            color = Color.YELLOW
            toPixelPosition(500f, 100f)
        }

        dummies += BitsEntity().apply {
            width = 75f
            height = 93f
            anchorX = 0f
            anchorY = 0f

            color = Color.GREEN
            toPixelPosition(300f, 100f)
        }
        val device = graphics.device
        val surfaceCapabilities = graphics.surfaceCapabilities
        val preferredFormat = graphics.preferredFormat

        graphics.configureSurface(
            TextureUsage.RENDER_ATTACHMENT,
            preferredFormat,
            PresentMode.FIFO,
            surfaceCapabilities.alphaModes[0]
        )

        val batch = SpriteBatch(device, graphics, preferredFormat)
        val shapeRenderer = ShapeRenderer(batch)
        val camera = OrthographicCamera(graphics.width, graphics.height)

        onResize { width, height ->
            camera.ortho(width, height)
            graphics.configureSurface(
                TextureUsage.RENDER_ATTACHMENT,
                preferredFormat,
                PresentMode.FIFO,
                surfaceCapabilities.alphaModes[0]
            )
        }

        onFixedUpdate {
            player.fixedUpdate()
            dummies.fastForEach { it.fixedUpdate() }
        }

        onUpdate { dt ->
            val surfaceTexture = graphics.surface.getCurrentTexture()
            when (val status = surfaceTexture.status) {
                TextureStatus.SUCCESS -> {
                    // all good, could check for `surfaceTexture.suboptimal` here.
                }

                TextureStatus.TIMEOUT,
                TextureStatus.OUTDATED,
                TextureStatus.LOST -> {
                    surfaceTexture.texture?.release()
                    logger.info { "getCurrentTexture status=$status" }
                    return@onUpdate
                }

                else -> {
                    // fatal
                    logger.fatal { "getCurrentTexture status=$status" }
                    close()
                    return@onUpdate
                }
            }
            val swapChainTexture = checkNotNull(surfaceTexture.texture)
            val frame = swapChainTexture.createView()

            val commandEncoder = device.createCommandEncoder()
            val renderPassEncoder =
                commandEncoder.beginRenderPass(
                    desc =
                    RenderPassDescriptor(
                        listOf(
                            RenderPassColorAttachmentDescriptor(
                                view = frame,
                                loadOp = LoadOp.CLEAR,
                                storeOp = StoreOp.STORE,
                                clearColor =
                                if (preferredFormat.srgb) Color.DARK_GRAY.toLinear()
                                else Color.DARK_GRAY
                            )
                        )
                    )
                )

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

            batch.use(renderPassEncoder, camera.viewProjection) {
                player.render(batch, camera, shapeRenderer)
                dummies.fastForEach { it.render(batch, camera, shapeRenderer) }
            }
            renderPassEncoder.end()

            val commandBuffer = commandEncoder.finish()

            device.queue.submit(commandBuffer)
            graphics.surface.present()

            commandBuffer.release()
            renderPassEncoder.release()
            commandEncoder.release()
            frame.release()
            swapChainTexture.release()
        }

        onRelease {
            batch.release()
            device.release()
        }
    }
}

suspend fun main() {
    createLittleKtApp {
        width = 960
        height = 540
    }.start {
        CollisionTest(it)
    }
}