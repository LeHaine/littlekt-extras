package com.lehaine.littlekt.extras.grid.entity

import com.lehaine.littlekt.math.castRay
import com.lehaine.littlekt.math.dist
import com.lehaine.littlekt.math.geom.radians
import kotlin.math.atan2
import kotlin.time.Duration


fun GridEntity.castRayTo(tcx: Int, tcy: Int, canRayPass: (Int, Int) -> Boolean) =
    castRay(cx, cy, tcx, tcy, canRayPass)

fun GridEntity.castRayTo(target: GridEntity, canRayPass: (Int, Int) -> Boolean) =
    castRay(cx, cy, target.cx, target.cy, canRayPass)

fun GridEntity.toGridPosition(cx: Int, cy: Int, xr: Float = 0.5f, yr: Float = 1f) {
    this.cx = cx
    this.cy = cy
    this.xr = xr
    this.yr = yr
    onPositionManuallyChanged()
}

fun GridEntity.toPixelPosition(x: Float, y: Float) {
    this.cx = (x / gridCellSize).toInt()
    this.cy = (y / gridCellSize).toInt()
    this.xr = (x - cx * gridCellSize) / gridCellSize
    this.yr = (y - cy * gridCellSize) / gridCellSize
    onPositionManuallyChanged()
}

fun GridEntity.dirTo(target: GridEntity) = dirTo(target.centerX)

fun GridEntity.dirTo(targetX: Float) = if (targetX > centerX) 1 else -1

fun GridEntity.distGridTo(tcx: Int, tcy: Int, txr: Float = 0.5f, tyr: Float = 0.5f) =
    dist(cx + xr, cy + yr, tcx + txr, tcy + tyr)

fun GridEntity.distGridTo(target: GridEntity) = distGridTo(target.cx, target.cy, target.xr, target.yr)

fun GridEntity.distPxTo(x: Float, y: Float) = dist(this.x, this.y, x, y)
fun GridEntity.distPxTo(targetGridPosition: GridEntity) = distPxTo(targetGridPosition.x, targetGridPosition.y)

fun GridEntity.angleTo(x: Float, y: Float) = atan2(y - this.y, x - this.x).radians
fun GridEntity.angleTo(target: GridEntity) = angleTo(target.centerX, target.centerY)

val GridEntity.cd get() = this.cooldown

fun GridEntity.cooldown(name: String, time: Duration, callback: () -> Unit = {}) =
    this.cooldown.timeout(name, time, callback)

fun GridEntity.cd(name: String, time: Duration, callback: () -> Unit = {}) =
    cooldown(name, time, callback)