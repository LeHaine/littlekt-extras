package com.lehaine.littlekt.extras.ecs.logic.collision.checker

import com.lehaine.littlekt.extras.GameLevel

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class LevelGroundChecker(private val level: GameLevel<*>) : GroundChecker() {
    override fun onGround(
        velocityY: Float,
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        collisionChecker: CollisionChecker
    ): Boolean {
        if (collisionChecker is LevelCollisionChecker) {
            return velocityY == 0f && level.hasCollision(
                cx,
                cy + 1
            ) && yr == collisionChecker.bottomCollisionRatio
        }
        return false
    }

}