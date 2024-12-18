package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
data class Move(
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var velocityZ: Float = 0f,
    var frictionX: Float = 0.82f,
    var frictionY: Float = 0.82f,
    var frictionZ: Float = 1f,
    override val poolType: PoolType<Move> = Move
) : PoolableComponent<Move> {

    override fun reset() {
        velocityX = 0f
        velocityY = 0f
        velocityZ = 0f
        frictionX = 0.82f
        frictionY = 0.82f
        frictionZ = 1f
    }

    override fun type(): ComponentType<Move> = Move

    companion object : ComponentType<Move>(), PoolType<Move> {
        override val poolName: String = "movePool"
    }
}