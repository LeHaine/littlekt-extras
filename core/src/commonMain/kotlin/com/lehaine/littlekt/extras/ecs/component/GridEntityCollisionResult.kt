package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.componentTypeOf

/**
 * @author Colton Daily
 * @date 12/22/2024
 */
class GridEntityCollisionResult(
    val collisionType: GridEntityCollisionType,
    override val poolType: PoolType<GridEntityCollisionResult> = when (collisionType) {
        GridEntityCollisionType.AABB -> AABBCollisionPool
        GridEntityCollisionType.INNER_CIRCLE -> InnerCircleCollisionPool
        GridEntityCollisionType.OUTER_CIRCLE -> OuterCircleCollisionPool
    }
) : PoolableComponent<GridEntityCollisionResult> {
    val collidingWith: MutableList<Grid> = mutableListOf()

    override fun reset() {
        collidingWith.clear()
    }

    override fun type(): ComponentType<GridEntityCollisionResult> = when (collisionType) {
        GridEntityCollisionType.AABB -> AABBCollision
        GridEntityCollisionType.INNER_CIRCLE -> InnerCircleCollision
        GridEntityCollisionType.OUTER_CIRCLE -> OuterCircleCollision
    }

    enum class GridEntityCollisionType {
        AABB,
        INNER_CIRCLE,
        OUTER_CIRCLE
    }

    companion object {
        val AABBCollision = componentTypeOf<GridEntityCollisionResult>()
        val InnerCircleCollision = componentTypeOf<GridEntityCollisionResult>()
        val OuterCircleCollision = componentTypeOf<GridEntityCollisionResult>()

        val AABBCollisionPool = poolTypeOf<GridEntityCollisionResult>("AABBCollision")
        val InnerCircleCollisionPool = poolTypeOf<GridEntityCollisionResult>("InnerCircleCollision")
        val OuterCircleCollisionPool = poolTypeOf<GridEntityCollisionResult>("OuterCircleCollision")
    }
}