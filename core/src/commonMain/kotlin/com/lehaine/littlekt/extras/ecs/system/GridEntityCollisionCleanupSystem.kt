package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollisionResult

/**
 * @author Colton Daily
 * @date 12/22/2024
 */
class GridEntityCollisionCleanupSystem : IteratingSystem(family = World.family {
    any(
        GridEntityCollisionResult.AABBCollision,
        GridEntityCollisionResult.InnerCircleCollision,
        GridEntityCollisionResult.OuterCircleCollision,
    )
}) {
    override fun onTickEntity(entity: Entity) {
        entity.configure { ctx ->
            entity.getOrNull(GridEntityCollisionResult.AABBCollision)?.let {
                ctx -= GridEntityCollisionResult.AABBCollision
            }
            entity.getOrNull(GridEntityCollisionResult.InnerCircleCollision)?.let {
                ctx -= GridEntityCollisionResult.InnerCircleCollision
            }
            entity.getOrNull(GridEntityCollisionResult.OuterCircleCollision)?.let {
                ctx -= GridEntityCollisionResult.OuterCircleCollision
            }
        }
    }
}