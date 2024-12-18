package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResult
import com.littlekt.util.datastructure.Pool

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionCleanupSystem(
    private val gridCollisionPool: Pool<GridCollisionResult>,
) : IteratingSystem(family = World.family {
    any(
        GridCollisionResult.GridCollisionX,
        GridCollisionResult.GridCollisionY
    )
}) {
    override fun onTickEntity(entity: Entity) {
        entity.configure { ctx ->
            entity.getOrNull(GridCollisionResult.GridCollisionX)?.let {
                ctx -= GridCollisionResult.GridCollisionX
                gridCollisionPool.free(it)
            }
            entity.getOrNull(GridCollisionResult.GridCollisionY)?.let {
                ctx -= GridCollisionResult.GridCollisionY
                gridCollisionPool.free(it)
            }
        }
    }
}