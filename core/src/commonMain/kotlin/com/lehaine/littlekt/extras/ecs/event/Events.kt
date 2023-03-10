package com.lehaine.littlekt.extras.ecs.event

sealed class LevelCollision(val x: Int, val y: Int) {
    object Left : LevelCollision(-1, 0)
    object Right : LevelCollision(1, 0)
    object Top : LevelCollision(0, -1)
    object Bottom : LevelCollision(0, 1)
}