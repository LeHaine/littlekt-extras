package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.lehaine.littlekt.extras.ecs.component.GridCollisionResult

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionCleanupSystem(
) : IteratingSystem(family = World.family {
    any(
        GridCollisionResult.GridCollisionX,
        GridCollisionResult.GridCollisionY,
        GridCollisionResult.GridCollisionZ
    )
}) {
    override fun onTickEntity(entity: Entity) {
        entity.configure { ctx ->
            entity.getOrNull(GridCollisionResult.GridCollisionX)?.let {
                ctx -= GridCollisionResult.GridCollisionX
            }
            entity.getOrNull(GridCollisionResult.GridCollisionY)?.let {
                ctx -= GridCollisionResult.GridCollisionY
            }
            entity.getOrNull(GridCollisionResult.GridCollisionZ)?.let {
                ctx -= GridCollisionResult.GridCollisionZ
            }
        }
    }
}