package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class Gravity(
    override val poolType: PoolType<Gravity> = Gravity
) : PoolableComponent<Gravity> {
    var gravityX: Float = 0f
    var gravityY: Float = 0f
    var gravityZ: Float = 0f

    var gravityMultiplier: Float = 1f

    var enableGravityX: Boolean = true
    var enableGravityY: Boolean = true
    var enableGravityZ: Boolean = true

    fun enableAll(enable: Boolean) {
        enableGravityX = enable
        enableGravityY = enable
        enableGravityZ = enable
    }

    override fun reset() {
        gravityX = 0f
        gravityY = 0f
        gravityZ = 0f
        gravityMultiplier = 1f
        enableGravityX = true
        enableGravityY = true
        enableGravityZ = true
    }

    override fun type(): ComponentType<Gravity> = Gravity

    fun calculateDeltaXGravity(): Float {
        return if (enableGravityX) {
            gravityMultiplier * gravityX
        } else {
            0f
        }
    }

    fun calculateDeltaYGravity(): Float {
        return if (enableGravityY) {
            gravityMultiplier * gravityY
        } else {
            0f
        }
    }

    fun calculateDeltaZGravity(): Float {
        return if (enableGravityZ) {
            gravityMultiplier * gravityZ
        } else {
            0f
        }
    }

    companion object : ComponentType<Gravity>(), PoolType<Gravity> {
        override val poolName: String = "gravityPool"
    }
}