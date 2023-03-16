package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.Interval
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.*
import com.lehaine.littlekt.util.datastructure.Pool
import kotlin.math.abs
import kotlin.math.ceil

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class GridMoveSystem(
    private val gridCollisionPool: Pool<GridCollisionResultComponent>? = null,
    interval: Interval = Fixed(1 / 30f)
) : IteratingSystem(family = family { all(MoveComponent, GridComponent) }, interval = interval) {

    override fun onTickEntity(entity: Entity) {
        val move = entity[MoveComponent]
        val grid = entity[GridComponent]

        val gravity = entity.getOrNull(GravityComponent)
        val collision = entity.getOrNull(GridCollisionComponent)

        grid.lastPx = grid.attachX
        grid.lastPy = grid.attachY

        if (gravity != null) {
            move.velocityX += gravity.calculateDeltaXGravity()
            move.velocityY += gravity.calculateDeltaYGravity()
        }

        /**
         * Any movement greater than [GridComponent.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val steps = ceil(abs(move.velocityX) + abs(move.velocityY) / grid.maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                grid.xr += move.velocityX / steps

                if (collision != null) {
                    if (move.velocityX != 0f) {
                        collision.checker.preXCheck(
                            grid.cx,
                            grid.cy,
                            grid.xr,
                            grid.yr,
                            move.velocityX,
                            move.velocityY,
                            grid.width,
                            grid.height,
                            grid.gridCellSize
                        )
                        val result = collision.checker.checkXCollision(
                            grid.cx,
                            grid.cy,
                            grid.xr,
                            grid.yr,
                            move.velocityX,
                            move.velocityY,
                            grid.width,
                            grid.height,
                            grid.gridCellSize
                        )
                        if (result != 0 && gridCollisionPool != null) {
                            entity.configure {
                                it += gridCollisionPool.alloc().apply {
                                    axes = GridCollisionResultComponent.Axes.X
                                    dir = result
                                }
                            }
                        }
                    }
                }

                while (grid.xr > 1) {
                    grid.xr--
                    grid.cx++
                }
                while (grid.xr < 0) {
                    grid.xr++
                    grid.cx--
                }

                grid.yr += move.velocityY / steps

                if (collision != null) {
                    if (move.velocityY != 0f) {
                        collision.checker.preYCheck(
                            grid.cx,
                            grid.cy,
                            grid.xr,
                            grid.yr,
                            move.velocityX,
                            move.velocityY,
                            grid.width,
                            grid.height,
                            grid.gridCellSize
                        )
                        val result = collision.checker.checkYCollision(
                            grid.cx,
                            grid.cy,
                            grid.xr,
                            grid.yr,
                            move.velocityX,
                            move.velocityY,
                            grid.width,
                            grid.height,
                            grid.gridCellSize
                        )
                        if (result != 0 && gridCollisionPool != null) {
                            entity.configure {
                                it += gridCollisionPool.alloc().apply {
                                    axes = GridCollisionResultComponent.Axes.Y
                                    dir = result
                                }
                            }
                        }
                    }
                }

                while (grid.yr > 1) {
                    grid.yr--
                    grid.cy++
                }

                while (grid.yr < 0) {
                    grid.yr++
                    grid.cy--
                }
                i++
            }
        }
        move.velocityX *= move.frictionX
        if (abs(move.velocityX) <= 0.0005f) {
            move.velocityX = 0f
        }

        move.velocityY *= move.frictionY
        if (abs(move.velocityY) <= 0.0005f) {
            move.velocityY = 0f
        }

        grid.zr += move.velocityZ

        if (grid.zr > 0 && gravity != null) {
            move.velocityZ -= gravity.calculateDeltaZGravity()
        }

        if (grid.zr < 0) {
            grid.zr = 0f
            move.velocityZ = -move.velocityZ * 0.9f
            if (abs(move.velocityZ) <= 0.06f) {
                move.velocityZ = 0f
            }
            if (gridCollisionPool != null) {
                entity.configure {
                    it += gridCollisionPool.alloc().apply {
                        axes = GridCollisionResultComponent.Axes.Z
                        dir = 0
                    }
                }
            }
        }

        move.velocityZ *= move.frictionZ
        if (abs(move.velocityZ) <= 0.0005f) {
            move.velocityZ = 0f
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        entity[GridComponent].interpolationAlpha = alpha
    }
}