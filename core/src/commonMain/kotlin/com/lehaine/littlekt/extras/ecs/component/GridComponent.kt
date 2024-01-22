package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.grid.entity.GridEntity
import com.lehaine.littlekt.math.castRay
import com.lehaine.littlekt.math.dist
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.interpolate
import com.lehaine.littlekt.util.seconds
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class GridComponent(var gridCellSize: Float, var width: Float = gridCellSize, var height: Float = gridCellSize) :
    Component<GridComponent> {
    var anchorX: Float = 0.5f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var anchorY: Float = 0.5f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }

    var cx: Int = 0
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var cy: Int = 0
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var xr: Float = 0.5f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var yr: Float = 1f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var zr: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }

    internal var dirty: Boolean = false

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
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var scaleY: Float = 1f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var rotation: Angle = Angle.ZERO
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    private var _stretchX = 1f
    private var _stretchY = 1f

    var stretchX: Float
        get() = _stretchX
        set(value) {
            _stretchX = value
            _stretchY = 2 - value
        }
    var stretchY: Float
        get() = _stretchY
        set(value) {
            _stretchX = 2 - value
            _stretchY = value
        }

    /**
     * The current entity x-scaling.
     */
    var entityScaleX = 1f

    /**
     * The current entity y-scaling.
     */
    var entityScaleY = 1f

    var restoreSpeed: Float = 12f

    var dir: Int = 1

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

    fun castRayTo(target: GridEntity, canRayPass: (Int, Int) -> Boolean) =
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

    fun updateScaling(dt: Duration) {
        entityScaleX = scaleX * dir * stretchX
        entityScaleY = scaleY * stretchY
        _stretchX += (1 - _stretchX) * min(1f, restoreSpeed * dt.seconds)
        _stretchY += (1 - _stretchY) * min(1f, restoreSpeed * dt.seconds)
    }

    override fun type(): ComponentType<GridComponent> = GridComponent

    companion object : ComponentType<GridComponent>()
}