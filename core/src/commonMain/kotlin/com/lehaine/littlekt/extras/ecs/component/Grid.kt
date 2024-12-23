package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.ComponentType
import com.littlekt.math.*
import com.littlekt.math.geom.Angle
import com.littlekt.math.geom.cosine
import com.littlekt.math.geom.radians
import com.littlekt.math.geom.sine
import com.littlekt.util.seconds
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class Grid(
    var gridCellSize: Float, var width: Float = gridCellSize, var height: Float = gridCellSize,
    override val poolType: PoolType<Grid> = Grid
) : PoolableComponent<Grid> {
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

    /**
     * The radius for the smallest encompassing circle for the rectangle. Used for SAT Collision checks.
     *
     * **Note**: this does use the Pythagorean theorem but rather an approximation formula. In other words, it doesn't
     * calculate a square root. This is good enough for our purposes.
     */
    val encompassingRadius get() = (7f / 8f * max(width, height) + min(width, height) * 0.5f) * 0.5f

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

    private val _topLeft = MutableVec2f()

    /**
     * Calculates the top-left vertex of the entity, used for SAT collision checking.
     */
    val topLeft: Vec2f
        get() = _topLeft.set(left, top).calculateVertex(centerX, centerY, rotation)

    private val _bottomLeft = MutableVec2f()

    /**
     * Calculates the bottom-left vertex of the entity, used for SAT collision checking.
     */
    val bottomLeft: Vec2f
        get() = _bottomLeft.set(left, bottom).calculateVertex(centerX, centerY, rotation)

    private val _bottomRight = MutableVec2f()

    /**
     * Calculates the bottom-right vertex of the entity, used for SAT collision checking.
     */
    val bottomRight: Vec2f
        get() = _bottomRight.set(right, bottom).calculateVertex(centerX, centerY, rotation)

    private val _topRight = MutableVec2f()

    /**
     * Calculates the top-right vertex of the entity, used for SAT collision checking.
     */
    val topRight: Vec2f
        get() = _topRight.set(right, top).calculateVertex(centerX, centerY, rotation)

    private val _vertices = MutableList(4) { Vec2f(0f) }
    val vertices: List<Vec2f>
        get() {
            _vertices[0] = topLeft
            _vertices[1] = bottomLeft
            _vertices[2] = bottomRight
            _vertices[3] = topRight
            return _vertices
        }

    private val _rect = MutableList(8) { 0f }
    private val rect: List<Float>
        get() {
            _rect[0] = vertices[0].x
            _rect[1] = vertices[0].y

            _rect[2] = vertices[1].x
            _rect[3] = vertices[1].y

            _rect[4] = vertices[2].x
            _rect[5] = vertices[2].y

            _rect[6] = vertices[3].x
            _rect[7] = vertices[3].y

            return _rect
        }

    fun castRayTo(tcx: Int, tcy: Int, canRayPass: (Int, Int) -> Boolean) =
        castRay(cx, cy, tcx, tcy, canRayPass)

    fun castRayTo(target: Grid, canRayPass: (Int, Int) -> Boolean) =
        castRay(cx, cy, target.cx, target.cy, canRayPass)

    fun toGridPosition(cx: Int, cy: Int, xr: Float = 0.5f, yr: Float = 1f) {
        this.cx = cx
        this.cy = cy
        this.xr = xr
        this.yr = yr
        onPositionManuallyChanged()
    }

    fun dirTo(target: Grid) = dirTo(target.centerX)

    fun dirTo(targetX: Float) = if (targetX > centerX) 1 else -1

    fun distGridTo(tcx: Int, tcy: Int, txr: Float = 0.5f, tyr: Float = 0.5f) =
        dist(cx + xr, cy + yr, tcx + txr, tcy + tyr)

    fun distGridTo(target: Grid) = distGridTo(target.cx, target.cy, target.xr, target.yr)

    fun distPxTo(x: Float, y: Float) = dist(this.x, this.y, x, y)
    fun distPxTo(target: Grid) = distPxTo(target.x, target.y)

    fun angleTo(x: Float, y: Float) = atan2(y - this.y, x - this.x).radians
    fun angleTo(target: Grid) = angleTo(target.centerX, target.centerY)

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

    private fun Grid.performSAT(poly2: List<Vec2f>): Boolean {
        val edges = tempVecList2
        var i = 0
        polyToEdges(vertices).forEach {
            edges[i].set(it)
            i++
        }

        polyToEdges(poly2).forEach {
            edges[i].set(it)
            i++
        }
        val axes = tempVecList3

        repeat(edges.size) { index ->
            axes[index].set(orthogonal(edges[index]))
        }

        for (axis in axes) {
            val projection1 = tempVec2f2.set(project(vertices, axis))
            val projection2 = tempVec2f3.set(project(poly2, axis))
            if (!overlap(projection1, projection2)) {
                return false
            }
        }

        return true
    }

    private fun edgeVector(v1: Vec2f, v2: Vec2f): Vec2f = tempVec2f.set(v2).subtract(v1)

    private fun polyToEdges(poly: List<Vec2f>): List<Vec2f> {
        repeat(poly.size) { index ->
            tempVecList[index].set(edgeVector(poly[index], poly[(index + 1) % poly.size]))
        }
        return tempVecList
    }

    private fun orthogonal(vec2f: Vec2f): Vec2f = tempVec2f.set(vec2f.y, -vec2f.x)

    private fun project(poly: List<Vec2f>, axis: Vec2f): Vec2f {
        repeat(poly.size) { index ->
            tempFloatList[index] = poly[index].dot(axis)
        }
        return tempVec2f.set(tempFloatList.min(), tempFloatList.max())
    }

    private fun overlap(projection1: Vec2f, projection2: Vec2f) =
        projection1.x <= projection2.y && projection2.x <= projection1.y

    /**
     * AABB check
     */
    fun isCollidingWith(from: Grid, useSat: Boolean = false): Boolean {
        if (useSat) {
            if (rotation != 0.radians || from.rotation != 0.radians) {
                if (!isCollidingWithEncompassingCircle(from)) return false
                return performSAT(from.vertices)
            }
        }

        // normal rectangle overlap check
        val lx = left
        val lx2 = from.left
        val rx = right
        val rx2 = from.right

        if (lx >= rx2 || lx2 >= rx) {
            return false
        }

        val ly = bottom
        val ry = top
        val ly2 = from.bottom
        val ry2 = from.top

        return !(ly >= ry2 || ly2 >= ry)
    }

    fun isCollidingWithInnerCircle(from: Grid): Boolean =
        isCollidingWithRadius(innerRadius, from, from.innerRadius)

    fun isCollidingWithOuterCircle(from: Grid): Boolean =
        isCollidingWithRadius(outerRadius, from, from.outerRadius)

    fun isCollidingWithEncompassingCircle(from: Grid): Boolean =
        isCollidingWithRadius(encompassingRadius, from, from.encompassingRadius)

    fun isCollidingWithRadius(radius: Float, from: Grid, fromRadius: Float): Boolean {
        val dist = radius + fromRadius
        return distSqr(centerX, centerY, from.centerX, from.centerY) <= dist * dist
    }

    override fun reset() {
        toGridPosition(0, 0)
        width = gridCellSize
        height = gridCellSize
        anchorX = 0.5f
        anchorY = 0.5f
        scaleX = 1f
        scaleY = 1f
        currentScaleX = 1f
        currentScaleY = 1f
    }

    private fun MutableVec2f.calculateVertex(cx: Float, cy: Float, angle: Angle): MutableVec2f {
        val px = x - cx
        val py = y - cy
        val sin = angle.sine
        val cos = angle.cosine
        val nx = px * cos - py * sin
        val ny = px * sin + py * cos
        set(nx + cx, ny + cy)
        return this
    }

    override fun type(): ComponentType<Grid> = Grid

    companion object : ComponentType<Grid>(), PoolType<Grid> {
        override val poolName: String = "gridPool"

        private val tempVec2f = MutableVec2f()
        private val tempVec2f2 = MutableVec2f()
        private val tempVec2f3 = MutableVec2f()
        private val tempVecList = MutableList(4) { MutableVec2f(0f) }
        private val tempVecList2 = MutableList(8) { MutableVec2f(0f) }
        private val tempVecList3 = MutableList(8) { MutableVec2f(0f) }
        private val tempFloatList = MutableList(4) { 0f }
    }
}