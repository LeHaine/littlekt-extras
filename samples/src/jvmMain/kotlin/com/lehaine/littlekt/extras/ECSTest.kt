package com.lehaine.littlekt.extras

import com.github.quillraven.fleks.*
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.extras.ecs.component.*
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.CollisionChecker
import com.lehaine.littlekt.extras.ecs.logic.collision.resolver.CollisionResolver
import com.lehaine.littlekt.extras.ecs.system.*
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.datastructure.Pool
import com.lehaine.littlekt.util.seconds
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class ECSTest(context: Context) : ContextListener(context) {

    private val gridCellSize = 16f

    override suspend fun Context.start() {
        val heroIdle = resourcesVfs["test/heroIdle0.png"].readTexture().slice()
        val batch = SpriteBatch(this)
        val viewport = ExtendViewport(240, 135)
        val gridCollisionPool = Pool { GridCollisionResultComponent(GridCollisionResultComponent.Axes.X, 0) }

        val world = configureWorld {
            systems {
                add(GridMoveSystem(gridCollisionPool))
                add(GridCollisionCleanupSystem(gridCollisionPool))

                add(PlayerMoveSystem())
                add(PlayerInputSystem(context.input))

                add(AnimationSystem())
                add(SpriteRenderBoundsCalculationSystem())
                add(RenderSystem(this@start, batch, viewport.camera, viewport))
            }
        }
        world.entity {
            it += SpriteComponent(heroIdle)
            it += RenderBoundsComponent()
            it += GridComponent(gridCellSize)
            it += GridCollisionComponent(SimpleCollisionChecker(5, 5))
            it += GridCollisionResolverComponent(SimpleCollisionResolver(5, 5))
            it += MoveComponent(frictionX = 0.82f, frictionY = 0.82f)
            it += PlayerInputComponent()
        }

        onResize { width, height ->
            viewport.update(width, height, this, true)
        }
        onRender { dt ->
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
            world.update(dt.seconds)
        }

        onPostRender {
            if (input.isKeyJustPressed(Key.P)) {
                println(stats)
            }
        }

        onDispose {
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
            grid: GridComponent,
            move: MoveComponent,
            collision: GridCollisionComponent,
            dir: Int
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
            grid: GridComponent,
            move: MoveComponent,
            collision: GridCollisionComponent,
            dir: Int
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

    private class RenderSystem(
        private val context: Context,
        private val batch: Batch,
        private val camera: Camera,
        private val viewport: Viewport
    ) : IteratingSystem(World.family { all(GridComponent, SpriteComponent) }) {

        private val viewBounds = Rect()

        override fun onTick() {
            viewport.apply(context)
            viewBounds.calculateViewBounds(camera)
            batch.use(camera.viewProjection) {
                super.onTick()
            }
        }

        override fun onTickEntity(entity: Entity) {
            val sprite = entity[SpriteComponent]
            val grid = entity[GridComponent]
            val renderBounds = entity.getOrNull(RenderBoundsComponent)

            val slice = sprite.slice

            if (slice != null) {
                if (renderBounds == null || viewBounds.intersects(renderBounds.bounds))
                    batch.draw(
                        slice,
                        grid.x,
                        grid.y,
                        grid.anchorX * slice.originalWidth,
                        grid.anchorY * slice.originalHeight,
                        width = sprite.renderWidth,
                        height = sprite.renderHeight,
                        scaleX = grid.scaleX,
                        scaleY = grid.scaleY,
                        flipX = sprite.flipX,
                        flipY = sprite.flipY,
                        rotation = grid.rotation,
                        colorBits = sprite.color.toFloatBits()
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
        IteratingSystem(World.family { all(PlayerInputComponent) }) {

        override fun onTickEntity(entity: Entity) {
            val playerInput = entity[PlayerInputComponent]

            playerInput.xMoveStrength = 0f
            playerInput.yMoveStrength = 0f

            if (input.isKeyPressed(Key.W)) {
                playerInput.yMoveStrength = -1f
            }
            if (input.isKeyPressed(Key.S)) {
                playerInput.yMoveStrength = 1f
            }
            if (input.isKeyPressed(Key.A)) {
                playerInput.xMoveStrength = -1f
            }
            if (input.isKeyPressed(Key.D)) {
                playerInput.xMoveStrength = 1f
            }
        }
    }

    private class PlayerMoveSystem :
        IteratingSystem(World.family { all(PlayerInputComponent, MoveComponent) }, interval = Fixed(1 / 30f)) {

        override fun onTickEntity(entity: Entity) {
            val playerInput = entity[PlayerInputComponent]
            val move = entity[MoveComponent]

            move.velocityX += playerInput.speed * playerInput.xMoveStrength
            move.velocityY += playerInput.speed * playerInput.yMoveStrength
        }
    }
}


fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
    }.start {
        ECSTest(it)
    }
}