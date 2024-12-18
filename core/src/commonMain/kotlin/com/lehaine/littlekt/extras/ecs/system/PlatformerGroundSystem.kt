package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.GridCollision
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.Move
import com.lehaine.littlekt.extras.ecs.component.Platformer

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerGroundSystem :
    IteratingSystem(family { all(Platformer, Grid, Move, GridCollision) }) {

    override fun onTickEntity(entity: Entity) {
        val platformer = entity[Platformer]
        val grid = entity[Grid]
        val move = entity[Move]
        val collision = entity[GridCollision]
        val checker = collision.checker

        platformer.onGround = platformer.groundChecker.onGround(
            move.velocityY,
            grid.cx,
            grid.cy,
            grid.xr,
            grid.yr,
            checker
        )

    }
}