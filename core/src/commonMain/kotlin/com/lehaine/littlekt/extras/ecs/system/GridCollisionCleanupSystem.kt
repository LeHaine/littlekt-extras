package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResultComponent
import com.lehaine.littlekt.util.datastructure.Pool

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionCleanupSystem(
    private val gridCollisionPool: Pool<GridCollisionResultComponent>,
) : IteratingSystem(family = World.family {
    any(
        GridCollisionResultComponent.GridCollisionX,
        GridCollisionResultComponent.GridCollisionY
    )
}) {
    override fun onTickEntity(entity: Entity) {
        entity.configure { ctx ->
            entity.getOrNull(GridCollisionResultComponent.GridCollisionX)?.let {
                ctx -= GridCollisionResultComponent.GridCollisionX
                gridCollisionPool.free(it)
            }
            entity.getOrNull(GridCollisionResultComponent.GridCollisionY)?.let {
                ctx -= GridCollisionResultComponent.GridCollisionY
                gridCollisionPool.free(it)
            }
        }
    }
}