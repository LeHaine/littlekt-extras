package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/17/2023
 */
class GridTransformSyncSystem : IteratingSystem(family { all(Grid) }) {

    override fun onTickEntity(entity: Entity) {
        entity[Grid].updateScaling(deltaTime.seconds)
    }
}