package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.interpolate
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

    val x: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }

    val y: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.interpolate(lastPy, attachY)
            } else {
                attachY
            }
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

    override fun type(): ComponentType<GridComponent> = GridComponent

    companion object : ComponentType<GridComponent>()
}