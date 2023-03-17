package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.GridComponent
import com.lehaine.littlekt.util.seconds

/**
 * @author Colton Daily
 * @date 3/17/2023
 */
class GridTransformSyncSystem : IteratingSystem(family { all(GridComponent) }) {

    override fun onTickEntity(entity: Entity) {
        entity[GridComponent].updateScaling(deltaTime.seconds)
    }
}