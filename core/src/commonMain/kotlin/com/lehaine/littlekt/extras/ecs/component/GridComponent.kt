package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.entity.Entity
import com.lehaine.littlekt.math.castRay
import com.lehaine.littlekt.math.dist
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.interpolate
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class GridComponent(var gridCellSize: Float, var width: Float = gridCellSize, var height: Float = gridCellSize) :
    Component<GridComponent> {
    var anchorX: Float = 0.5f
    var anchorY: Float = 0.5f

    var cx: Int = 0
    var cy: Int = 0
    var xr: Float = 0.5f
    var yr: Float = 1f
    var zr: Float = 0f

    var maxGridMovementPercent: Float = 0.33f

    val innerRadius get() = min(width, height) * 0.5f
    val outerRadius get() = max(width, height) * 0.5f

    var interpolatePixelPosition: Boolean = true

    var interpolationAlpha: Float = 1f

    var lastPx: Float = 0f
    var lastPy: Float = 0f

    var x: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }
        set(value) {
            cx = (value / gridCellSize).toInt()
            xr = (value - cx * gridCellSize) / gridCellSize
            onPositionManuallyChanged()
        }

    var y: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.interpolate(lastPy, attachY)
            } else {
                attachY
            }
        }
        set(value) {
            cy = (value / gridCellSize).toInt()
            yr = (value - cy * gridCellSize) / gridCellSize
            onPositionManuallyChanged()
        }

    var scaleX: Float = 1f
    var scaleY: Float = 1f
    var rotation: Angle = Angle.ZERO

    val attachX get() = (cx + xr) * gridCellSize
    val attachY get() = (cy + yr - zr) * gridCellSize
    val centerX get() = attachX + (0.5f - anchorX) * width
    val centerY get() = attachY + (0.5f - anchorY) * height
    val top get() = attachY - anchorY * height
    val right get() = attachX + (1 - anchorX) * width
    val bottom get() = attachY + (1 - anchorY) * height
    val left get() = attachX - anchorX * width

    fun castRayTo(tcx: Int, tcy: Int, canRayPass: (Int, Int) -> Boolean) =
        castRay(cx, cy, tcx, tcy, canRayPass)

    fun castRayTo(target: Entity, canRayPass: (Int, Int) -> Boolean) =
        castRay(cx, cy, target.cx, target.cy, canRayPass)

    fun toGridPosition(cx: Int, cy: Int, xr: Float = 0.5f, yr: Float = 1f) {
        this.cx = cx
        this.cy = cy
        this.xr = xr
        this.yr = yr
        onPositionManuallyChanged()
    }

    fun dirTo(target: GridComponent) = dirTo(target.centerX)

    fun dirTo(targetX: Float) = if (targetX > centerX) 1 else -1

    fun distGridTo(tcx: Int, tcy: Int, txr: Float = 0.5f, tyr: Float = 0.5f) =
        dist(cx + xr, cy + yr, tcx + txr, tcy + tyr)

    fun distGridTo(target: GridComponent) = distGridTo(target.cx, target.cy, target.xr, target.yr)

    fun distPxTo(x: Float, y: Float) = dist(this.x, this.y, x, y)
    fun distPxTo(target: GridComponent) = distPxTo(target.x, target.y)

    fun angleTo(x: Float, y: Float) = atan2(y - this.y, x - this.x).radians
    fun angleTo(target: GridComponent) = angleTo(target.centerX, target.centerY)

    fun onPositionManuallyChanged() {
        lastPx = attachX
        lastPy = attachY
    }

    override fun type(): ComponentType<GridComponent> = GridComponent

    companion object : ComponentType<GridComponent>()
}