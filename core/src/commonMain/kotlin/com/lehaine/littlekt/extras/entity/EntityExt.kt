package com.lehaine.littlekt.extras.entity

import com.lehaine.littlekt.math.castRay
import com.lehaine.littlekt.math.dist
import com.lehaine.littlekt.math.geom.radians
import kotlin.math.atan2
import kotlin.time.Duration


fun Entity.castRayTo(tcx: Int, tcy: Int, canRayPass: (Int, Int) -> Boolean) =
    castRay(cx, cy, tcx, tcy, canRayPass)

fun Entity.castRayTo(target: Entity, canRayPass: (Int, Int) -> Boolean) =
    castRay(cx, cy, target.cx, target.cy, canRayPass)

fun Entity.toGridPosition(cx: Int, cy: Int, xr: Float = 0.5f, yr: Float = 1f) {
    this.cx = cx
    this.cy = cy
    this.xr = xr
    this.yr = yr
    onPositionManuallyChanged()
}

fun Entity.toPixelPosition(x: Float, y: Float) {
    this.cx = (x / gridCellSize).toInt()
    this.cy = (y / gridCellSize).toInt()
    this.xr = (x - cx * gridCellSize) / gridCellSize
    this.yr = (y - cy * gridCellSize) / gridCellSize
    onPositionManuallyChanged()
}

fun Entity.dirTo(target: Entity) = dirTo(target.centerX)

fun Entity.dirTo(targetX: Float) = if (targetX > centerX) 1 else -1

fun Entity.distGridTo(tcx: Int, tcy: Int, txr: Float = 0.5f, tyr: Float = 0.5f) =
    dist(cx + xr, cy + yr, tcx + txr, tcy + tyr)

fun Entity.distGridTo(target: Entity) = distGridTo(target.cx, target.cy, target.xr, target.yr)

fun Entity.distPxTo(x: Float, y: Float) = dist(this.x, this.y, x, y)
fun Entity.distPxTo(targetGridPosition: Entity) = distPxTo(targetGridPosition.x, targetGridPosition.y)

fun Entity.angleTo(x: Float, y: Float) = atan2(y - this.y, x - this.x).radians
fun Entity.angleTo(target: Entity) = angleTo(target.centerX, target.centerY)

val Entity.cd get() = this.cooldown

fun Entity.cooldown(name: String, time: Duration, callback: () -> Unit = {}) =
    this.cooldown.timeout(name, time, callback)

fun Entity.cd(name: String, time: Duration, callback: () -> Unit = {}) =
    cooldown(name, time, callback)