package com.lehaine.littlekt.extras.grid.entity

import com.lehaine.littlekt.extras.GameLevel

open class GridPlatformEntity(level: GameLevel<*>, gridCellSize: Float) : GridLevelEntity(level, gridCellSize) {
    val onGround: Boolean
        get() = velocityY == 0f && level.hasCollision(
            cx,
            cy - 1
        ) && yr == bottomCollisionRatio

    var hasGravity: Boolean = true

    private val gravityPulling: Boolean get() = !onGround && hasGravity

    init {
        gravityY = -0.075f
    }

    override fun calculateDeltaYGravity(): Float {
        return if (gravityPulling) {
            gravityMultiplier * gravityY
        } else {
            0f
        }
    }
}