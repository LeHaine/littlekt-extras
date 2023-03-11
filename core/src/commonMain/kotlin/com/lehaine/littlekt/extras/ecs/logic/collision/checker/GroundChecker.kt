package com.lehaine.littlekt.extras.ecs.logic.collision.checker

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
abstract class GroundChecker {

    abstract fun onGround(
        velocityY: Float,
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        collisionChecker: CollisionChecker
    ): Boolean
}