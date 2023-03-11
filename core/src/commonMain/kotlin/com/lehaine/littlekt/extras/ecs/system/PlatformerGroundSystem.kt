package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.GameLevel
import com.lehaine.littlekt.extras.ecs.component.GridCollisionComponent
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.extras.ecs.component.MoveComponent
import com.lehaine.littlekt.extras.ecs.component.PlatformerComponent
import com.lehaine.littlekt.extras.ecs.logic.collision.checker.LevelCollisionChecker

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerGroundSystem(private val level: GameLevel<*>) :
    IteratingSystem(family { all(PlatformerComponent, GridComponent, MoveComponent, GridCollisionComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val platformer = entity[PlatformerComponent]
        val grid = entity[GridComponent]
        val move = entity[MoveComponent]
        val collision = entity[GridCollisionComponent]
        val checker = collision.checker

        if (checker is LevelCollisionChecker) {
            platformer.onGround = move.velocityY == 0f && level.hasCollision(
                grid.cx,
                grid.cy + 1
            ) && grid.yr == checker.bottomCollisionRatio
        }
    }
}