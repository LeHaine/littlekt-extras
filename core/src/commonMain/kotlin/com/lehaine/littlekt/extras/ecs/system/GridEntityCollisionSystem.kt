package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.Interval
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollision
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollisionResult

/**
 * @author Colton Daily
 * @date 12/22/2024
 */
class GridEntityCollisionSystem(
    interval: Interval = Fixed(1 / 30f)
) : IteratingSystem(family = family { all(Grid, GridEntityCollision) }, interval = interval) {

    override fun onTickEntity(entity: Entity) {
        val grid = entity[Grid]
        val useSat = entity[GridEntityCollision].useSat

        family.forEach { targetEntity ->
            if (targetEntity == entity) return@forEach
            val targetGrid = targetEntity[Grid]
            // if we aren't colliding with outer circle then we aren't colliding with inner or aabb
            if (grid.isCollidingWithOuterCircle(targetGrid)) {
                entity.addOuterCollision(targetGrid)
                targetEntity.addOuterCollision(grid)

                if (grid.isCollidingWithInnerCircle(targetGrid)) {
                    entity.addInnerCollision(targetGrid)
                    targetEntity.addInnerCollision(grid)
                } else if (grid.isCollidingWith(targetGrid, useSat)) {
                    entity.addAABBCollision(targetGrid)
                    targetEntity.addAABBCollision(grid)
                }
            }
        }
    }

    private fun Entity.addAABBCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.AABBCollision)
            ?: GridEntityCollisionResult.AABBCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }

    private fun Entity.addOuterCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.OuterCircleCollision)
            ?: GridEntityCollisionResult.OuterCircleCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }

    private fun Entity.addInnerCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.InnerCircleCollision)
            ?: GridEntityCollisionResult.InnerCircleCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }
}