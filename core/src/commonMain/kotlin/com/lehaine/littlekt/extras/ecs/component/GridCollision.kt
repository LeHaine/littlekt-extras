package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.CollisionChecker

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
open class GridCollision(
    var checker: CollisionChecker = CollisionChecker(),
    override val poolType: PoolType<GridCollision> = EmptyCollisionCheckerPool
) : PoolableComponent<GridCollision> {

    override fun type(): ComponentType<GridCollision> = GridCollision

    override fun reset() {
        // no op
    }

    companion object : ComponentType<GridCollision>() {
        val EmptyCollisionCheckerPool = poolTypeOf<GridCollision>("EmptyCollisionCheckerPool")
        val LevelCollisionCheckerPool = poolTypeOf<GridCollision>("LevelCollisionCheckerPool")
        val ObliqueCollisionCheckerPool = poolTypeOf<GridCollision>("ObliqueCollisionCheckerPool")
    }

}