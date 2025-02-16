package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.InjectableConfiguration
import com.github.quillraven.fleks.componentTypeOf

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResult(
    var axes: Axes,
    var dir: Int,
    override val poolType: PoolType<GridCollisionResult> = when (axes) {
        Axes.X -> GridCollisionXPool
        Axes.Y -> GridCollisionYPool
        Axes.Z -> GridCollisionZPool
    }
) : PoolableComponent<GridCollisionResult> {
    enum class Axes { X, Y, Z }

    override fun reset() {
        // no op
    }

    override fun type(): ComponentType<GridCollisionResult> = when (axes) {
        Axes.X -> GridCollisionX
        Axes.Y -> GridCollisionY
        Axes.Z -> GridCollisionZ
    }

    companion object {
        val GridCollisionX = componentTypeOf<GridCollisionResult>()
        val GridCollisionY = componentTypeOf<GridCollisionResult>()
        val GridCollisionZ = componentTypeOf<GridCollisionResult>()

        val GridCollisionXPool = poolTypeOf<GridCollisionResult>("GridCollisionX")
        val GridCollisionYPool = poolTypeOf<GridCollisionResult>("GridCollisionY")
        val GridCollisionZPool = poolTypeOf<GridCollisionResult>("GridCollisionZ")

        fun InjectableConfiguration.addGridCollisionResultPools() {
            addPool(GridCollisionXPool) {
                GridCollisionResult(Axes.X, 0)
            }
            addPool(GridCollisionYPool) {
                GridCollisionResult(Axes.Y, 0)
            }
            addPool(GridCollisionZPool) {
                GridCollisionResult(Axes.Z, 0)
            }
        }
    }
}