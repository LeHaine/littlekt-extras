package com.lehaine.littlekt.extras

import com.github.quillraven.fleks.*
import com.lehaine.littlekt.extras.ecs.component.*
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResult.Companion.addGridCollisionResultPools
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollisionResult.Companion.addGridEntityCollisionResultPools
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.CollisionChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.GroundChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.CollisionResolver
import com.lehaine.littlekt.extras.ecs.system.*
import com.littlekt.Context
import com.littlekt.ContextListener
import com.littlekt.createLittleKtApp
import com.littlekt.file.vfs.readTexture
import com.littlekt.graphics.Camera
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.SpriteBatch
import com.littlekt.graphics.g2d.use
import com.littlekt.graphics.slice
import com.littlekt.graphics.webgpu.*
import com.littlekt.input.Input
import com.littlekt.input.Key
import com.littlekt.log.Logger
import com.littlekt.math.Rect
import com.littlekt.util.calculateViewBounds
import com.littlekt.util.seconds
import com.littlekt.util.viewport.ExtendViewport

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class ECSPlatformerTest(context: Context) : ContextListener(context) {

    private val gridCellSize = 16f

    override suspend fun Context.start() {
        val heroIdle = resourcesVfs["test/heroIdle0.png"].readTexture().slice()
        val batch = SpriteBatch(context.graphics.device, context.graphics, graphics.preferredFormat)
        val viewport = ExtendViewport(240, 135)

        val world = configureWorld {
            injectables {
                addGridCollisionResultPools()
                addGridEntityCollisionResultPools()
            }
            systems {
                add(PlatformerGroundSystem())
                add(PlatformerGravitySystem())

                add(PlayerMoveSystem())
                add(PlayerInputSystem(context.input))

                add(GridMoveSystem())
                add(GridCollisionCleanupSystem())

                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(RenderSystem(this@start, batch, viewport.camera))
            }
        }
        world.entity {
            it += Sprite(heroIdle)
            it += RenderBounds()
            it += Grid(gridCellSize).apply {
                anchorY = 0f
            }
            it += GridCollision(SimpleCollisionChecker(5, 5))
            it += GridCollisionResolver(SimpleCollisionResolver(5, 5))
            it += Gravity().apply {
                gravityY = -0.075f
            }
            it += Platformer(SimpleGroundChecker())
            it += Move(frictionX = 0.82f, frictionY = 0.82f)
            it += PlayerInputComponent()
        }

        onResize { width, height ->
            viewport.update(width, height, centerCamera = true)
            graphics.configureSurface(
                TextureUsage.RENDER_ATTACHMENT,
                graphics.preferredFormat,
                PresentMode.FIFO,
                graphics.surfaceCapabilities.alphaModes[0]
            )
        }
        onUpdate { dt ->
            world.update(dt.seconds)
        }

        onPostUpdate {
            if (input.isKeyJustPressed(Key.P)) {
                println(stats)
            }
        }

        onRelease {
            world.dispose()
        }
    }

    private class SimpleCollisionChecker(val gridWidth: Int, val gridHeight: Int) : CollisionChecker() {
        override fun checkXCollision(
            cx: Int,
            cy: Int,
            xr: Float,
            yr: Float,
            velocityX: Float,
            velocityY: Float,
            width: Float,
            height: Float,
            cellSize: Float
        ): Int {
            if (cx - 1 < 0 && xr <= 0.3f) {
                return -1
            }
            if (cx + 1 > gridWidth && xr >= 0.7f) {
                return 1
            }
            return 0
        }

        override fun checkYCollision(
            cx: Int,
            cy: Int,
            xr: Float,
            yr: Float,
            velocityX: Float,
            velocityY: Float,
            width: Float,
            height: Float,
            cellSize: Float
        ): Int {
            if (cy - 1 < 0 && yr <= 0.3f) {
                return -1
            }
            if (cy + 1 > gridHeight && yr >= 0.7f) {
                return 1
            }
            return 0
        }
    }

    private class SimpleCollisionResolver(val gridWidth: Int, val gridHeight: Int) : CollisionResolver() {

        override fun resolveXCollision(
            grid: Grid, move: Move, collision: GridCollision, dir: Int
        ) {
            if (dir == -1) {
                grid.cx = 0
                grid.xr = 0.3f
            }
            if (dir == 1) {
                grid.cx = gridWidth
                grid.xr = 0.7f
            }
        }

        override fun resolveYCollision(
            grid: Grid, move: Move, collision: GridCollision, dir: Int
        ) {
            if (dir == -1) {
                grid.cy = 0
                grid.yr = 0.3f
            }
            if (dir == 1) {
                grid.cy = gridHeight
                grid.yr = 0.7f
            }
        }
    }

    private class SimpleGroundChecker : GroundChecker() {
        override fun onGround(
            velocityY: Float, cx: Int, cy: Int, xr: Float, yr: Float, collisionChecker: CollisionChecker
        ): Boolean {
            return cy == 0 && yr <= 0.3f
        }
    }

    private class RenderSystem(
        context: Context,
        private val batch: Batch,
        private val camera: Camera,
    ) : IteratingSystem(World.family { all(Grid, Sprite) }) {

        private val viewBounds = Rect()
        private val graphics = context.graphics
        private val device = graphics.device
        private val preferredFormat = graphics.preferredFormat
        private val logger = Logger<RenderSystem>()

        override fun onTick() {
            val surfaceTexture = graphics.surface.getCurrentTexture()
            when (val status = surfaceTexture.status) {
                TextureStatus.SUCCESS -> {
                    // all good, could check for `surfaceTexture.suboptimal` here.
                }

                TextureStatus.TIMEOUT, TextureStatus.OUTDATED, TextureStatus.LOST -> {
                    surfaceTexture.texture?.release()
                    logger.info { "getCurrentTexture status=$status" }
                    return
                }

                else -> {
                    // fatal
                    error("getCurrentTexture status=$status")
                }
            }
            val swapChainTexture = checkNotNull(surfaceTexture.texture)
            val frame = swapChainTexture.createView()

            val commandEncoder = device.createCommandEncoder()
            val renderPassEncoder = commandEncoder.beginRenderPass(
                desc = RenderPassDescriptor(
                    listOf(
                        RenderPassColorAttachmentDescriptor(
                            view = frame,
                            loadOp = LoadOp.CLEAR,
                            storeOp = StoreOp.STORE,
                            clearColor = if (preferredFormat.srgb) Color.DARK_GRAY.toLinear()
                            else Color.DARK_GRAY
                        )
                    )
                )
            )

            camera.update()
            viewBounds.calculateViewBounds(camera)
            batch.use(renderPassEncoder, camera.viewProjection) {
                super.onTick()
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

        override fun onTickEntity(entity: Entity) {
            val sprite = entity[Sprite]
            val grid = entity[Grid]
            val renderBounds = entity.getOrNull(RenderBounds)

            val slice = sprite.slice

            if (slice != null) {
                if (renderBounds == null || viewBounds.intersects(renderBounds.bounds)) batch.draw(
                    slice,
                    grid.x,
                    grid.y,
                    grid.anchorX * slice.actualWidth,
                    grid.anchorY * slice.actualHeight,
                    width = sprite.renderWidth,
                    height = sprite.renderHeight,
                    scaleX = grid.scaleX,
                    scaleY = grid.scaleY,
                    flipX = sprite.flipX,
                    flipY = sprite.flipY,
                    rotation = grid.rotation,
                    color = sprite.color
                )

            }
        }
    }

    private class PlayerInputComponent : Component<PlayerInputComponent> {
        var speed = 0.03f
        var xMoveStrength = 0f
        var yMoveStrength = 0f

        override fun type() = PlayerInputComponent

        companion object : ComponentType<PlayerInputComponent>()
    }

    private class PlayerInputSystem(private val input: Input) :
        IteratingSystem(World.family { all(PlayerInputComponent, Move) }) {

        override fun onTickEntity(entity: Entity) {
            val playerInput = entity[PlayerInputComponent]
            val move = entity[Move]

            playerInput.xMoveStrength = 0f
            playerInput.yMoveStrength = 0f

            if (input.isKeyPressed(Key.A)) {
                playerInput.xMoveStrength = -1f
            }
            if (input.isKeyPressed(Key.D)) {
                playerInput.xMoveStrength = 1f
            }

            if (input.isKeyJustPressed(Key.SPACE)) {
                move.velocityY = 0.5f
            }
        }
    }

    private class PlayerMoveSystem :
        IteratingSystem(World.family { all(PlayerInputComponent, Move) }, interval = Fixed(1 / 30f)) {

        override fun onTickEntity(entity: Entity) {
            val playerInput = entity[PlayerInputComponent]
            val move = entity[Move]

            move.velocityX += playerInput.speed * playerInput.xMoveStrength
            move.velocityY += playerInput.speed * playerInput.yMoveStrength
        }
    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
    }.start {
        ECSPlatformerTest(it)
    }
}