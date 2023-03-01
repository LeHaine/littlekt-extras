package com.lehaine.littlekt.extras.entity

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.g2d.tilemap.ldtk.LDtkEntity
import com.lehaine.littlekt.extras.GameLevel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.floor

open class LevelEntity(
    protected open val level: GameLevel<*>,
    gridCellSize: Float
) : Entity(gridCellSize) {
    open var rightCollisionRatio: Float = 0.7f
    open var leftCollisionRatio: Float = 0.3f
    open var bottomCollisionRatio: Float = 1f
    open var topCollisionRatio: Float = 1f
    open var useTopCollisionRatio: Boolean = false

    fun setFromLevelEntity(data: LDtkEntity) {
        cx = data.cx
        cy = data.cy
        xr = data.pivotX
        yr = data.pivotY
        anchorX = data.pivotX
        anchorY = data.pivotY
    }

    override fun checkXCollision() {
        if (level.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            xr = rightCollisionRatio
            velocityX *= 0.5f
            onLevelCollision(1, 0)
        }

        if (level.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            xr = leftCollisionRatio
            velocityX *= 0.5f
            onLevelCollision(-1, 0)
        }
    }

    override fun checkYCollision() {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize)
        if (level.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY = 0f
            onLevelCollision(0, -1)
        }
        if (level.hasCollision(cx, cy + 1) && yr >= bottomCollisionRatio) {
            velocityY = 0f
            yr = bottomCollisionRatio
            onLevelCollision(0, 1)
        }
    }

    open fun onLevelCollision(xDir: Int, yDir: Int) = Unit
}