package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.grid.entity.GridEntity
import com.littlekt.math.castRay
import com.littlekt.math.dist
import com.littlekt.math.geom.Angle
import com.littlekt.math.geom.radians
import com.littlekt.math.interpolate
import com.littlekt.util.seconds
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
    var anchorY: Float = 0f
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
    var yr: Float = 0f
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

    /**
     * The ratio to interpolate the last position to the new position.
     * This will need updated before each update.
     */
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
    private var _squashX = 1f
    private var _squashY = 1f

    var squashX: Float
        get() = _squashX
        set(value) {
            _squashX = value
            _squashY = 2 - value
        }
    var squashY: Float
        get() = _squashY
        set(value) {
            _squashX = 2 - value
            _squashY = value
        }

    /**
     * The current x-scaling.
     */
    var currentScaleX = 1f

    /**
     * The current y-scaling.
     */
    var currentScaleY = 1f

    var restoreSpeed: Float = 12f

    var dir: Int = 1

    val attachX get() = (cx + xr) * gridCellSize
    val attachY get() = (cy + yr - zr) * gridCellSize
    val centerX get() = attachX + (0.5f - anchorX) * width
    val centerY get() = attachY + (0.5f - anchorY) * height
    val top get() = attachY + (1 - anchorY) * height
    val right get() = attachX + (1 - anchorX) * width
    val bottom get() = attachY - anchorY * height
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
        currentScaleX = scaleX * dir * squashX
        currentScaleY = scaleY * squashY
        _squashX += (1 - _squashX) * min(1f, restoreSpeed * dt.seconds)
        _squashY += (1 - _squashY) * min(1f, restoreSpeed * dt.seconds)
    }

    override fun type(): ComponentType<GridComponent> = GridComponent

    companion object : ComponentType<GridComponent>()
}