package com.lehaine.littlekt.extras.grid.entity

import com.lehaine.littlekt.extras.Cooldown
import com.lehaine.littlekt.extras.renderable.AnimatedSprite
import com.littlekt.graphics.Camera
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.math.MutableVec2f
import com.littlekt.math.Vec2f
import com.littlekt.math.distSqr
import com.littlekt.math.geom.Angle
import com.littlekt.math.geom.cosine
import com.littlekt.math.geom.radians
import com.littlekt.math.geom.sine
import com.littlekt.math.interpolate
import com.littlekt.util.seconds
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class EntityDslMarker

@OptIn(ExperimentalContracts::class)
fun gridEntity(gridCellSize: Float, callback: @EntityDslMarker GridEntity.() -> Unit = {}): GridEntity {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return GridEntity(gridCellSize).also(callback)
}

/**
 * An entity that uses a Grid/Cell positioning system.
 */
open class GridEntity(val gridCellSize: Float) {

    /**
     * The [AnimatedSprite] of this entity.
     */
    val sprite = AnimatedSprite()

    /**
     * The current sprite anchor on the x-axis. Used for rendering and positioning calculations.
     */
    var anchorX: Float
        get() = sprite.anchorX
        set(value) {
            sprite.anchorX = value
        }

    /**
     * The current sprite anchor on the y-axis. Used for rendering and positioning calculations.
     */
    var anchorY: Float
        get() = sprite.anchorY
        set(value) {
            sprite.anchorY = value
        }

    /**
     * The grid-x position
     */
    var cx: Int = 0

    /**
     * The grid-y position
     */
    var cy: Int = 0

    /**
     * The x-ratio of how far through the current grid cell the entity is.
     * E.g. `0.5` is halfway through the cell. `1f` is on the right side and `0f` is on the left side.
     */
    var xr: Float = 0.5f

    /**
     * The y-ratio of how far through the current grid cell the entity is.
     * E.g. `0.5` is halfway through the cell. `1f` is on the top side and `0f` is bottom side.
     */
    var yr: Float = 0f

    /**
     * The force of gravity on the x-axis.
     */
    var gravityX: Float = 0f

    /**
     * The force of gravity on the y-axis.
     */
    var gravityY: Float = 0f

    /**
     * The value to multiply [gravityX] and [gravityY] by in order to increase or decrease gravity forces.
     */
    var gravityMultiplier: Float = 1f

    /**
     * The velocity on the x-axis.
     */
    var velocityX: Float = 0f

    /**
     * The velocity on the y-axis.
     */
    var velocityY: Float = 0f

    /**
     * The friction amount on the x-axis, which affects [velocityX].
     */
    var frictionX: Float = 0.82f

    /**
     * The friction amount on the y-axis, which affect [velocityY]
     */
    var frictionY: Float = 0.82f

    /**
     * Any movement greater than this value will increase the number of steps checked between movement.
     * The more steps will break down the movement into smaller pieces to avoid skipping grid collissions.
     */
    var maxGridMovementPercent: Float = 0.33f

    var width: Float = gridCellSize
    var height: Float = gridCellSize

    /**
     * Takes the smallest of either [width] or [height] and halves it.
     */
    val innerRadius get() = min(width, height) * ppuInv * 0.5f

    /**
     * Takes the largest of either [width] or [height] and halves it.
     */
    val outerRadius get() = max(width, height) * ppuInv * 0.5f

    /**
     * The radius for the smallest encompassing circle for the rectangle. Used for SAT Collision checks.
     *
     * **Note**: this does use the Pythagorean theorem but rather an approximation formula. In other words, it doesn't
     * calculate a square root. This is good enough for our purposes.
     */
    val encompassingRadius get() = (7f / 8f * max(width, height) + min(width, height) * 0.5f) * ppuInv * 0.5f

    var interpolatePixelPosition: Boolean = true
    var lastPx: Float = 0f
    var lastPy: Float = 0f

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
     * Extra scaling that is used to calculate [scaleX]
     */
    var extraScaleX = 1f

    /**
     * Extra scaling that is used to calculate [scaleY].
     */
    var extraScaleY = 1f

    /**
     * How fast the entity restores itself from beeing streched with [stretchX] and [stretchY].
     */
    var restoreSpeed: Float = 12f

    /**
     * The horizontal direction the entity is current facing. Typically `-1` for **left** and `1` for **right**.
     */
    var dir: Int = 1

    /**
     * Pixels-per-unit, defaults to 1f.
     */
    var ppu: Float = 1f
        set(value) {
            field = value
            sprite.ppu = value
        }

    /**
     * The inverse of [ppu].
     */
    val ppuInv: Float get() = 1f / ppu

    /**
     * The ratio to interpolate the last position to the new position.
     * This will need updated before each update.
     */
    var fixedProgressionRatio = 1f

    /**
     * The current x-position of the entity. If [interpolatePixelPosition] is `true`, then this value is interpolated from
     * [lastPx] to [attachX] using the [fixedProgressionRatio]. Otherwise, it just returned [attachX].
     */
    val x: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }

    /**
     * The current y-position of the entity. If [interpolatePixelPosition] is `true`, then this value is interpolated from
     * [lastPy] to [attachY] using the [fixedProgressionRatio]. Otherwise, it just returned [attachY].
     */
    val y: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPy, attachY)
            } else {
                attachY
            }
        }

    /**
     * The x-scale of the entity.
     */
    var scaleX: Float = 1f

    /**
     * The y-scale of the entity.
     */
    var scaleY: Float = 1f

    /**
     * The rotation of the entity.
     */
    var rotation: Angle = Angle.ZERO

    /**
     * The x-position of the attachment of the entity. The value is calculated from the grid position: [cx] and [xr].
     */
    open val attachX get() = ((cx + xr) * gridCellSize) * ppuInv

    /**
     * The y-position of the attachment of the entity. The value is calculated from the grid position: [cy] and [yr].
     */
    open val attachY get() = ((cy + yr) * gridCellSize) * ppuInv
    val centerX get() = attachX + (0.5f - anchorX) * width
    val centerY get() = attachY + (0.5f - anchorY) * height
    val top get() = attachY + (1 - anchorY) * height * ppuInv
    val right get() = attachX + (1 - anchorX) * width * ppuInv
    val bottom get() = attachY - anchorY * height * ppuInv
    val left get() = attachX - anchorX * width * ppuInv

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
    private val vertices: List<Vec2f>
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

    val cooldown = Cooldown()

    var destroyed: Boolean = false

    init {
        anchorX = 0.5f
        anchorY = 1f

        @Suppress("LeakingThis") updateGridPosition()
    }

    open fun preUpdate(dt: Duration) {
        cd.update(dt)
    }

    open fun fixedUpdate() {
        updateGridPosition()
    }

    open fun update(dt: Duration) {
        sprite.update(dt)
    }

    open fun postUpdate(dt: Duration) {
        sprite.x = x
        sprite.y = y
        extraScaleX = scaleX * dir * stretchX
        extraScaleY = scaleY * stretchY
        _stretchX += (1 - _stretchX) * min(1f, restoreSpeed * dt.seconds)
        _stretchY += (1 - _stretchY) * min(1f, restoreSpeed * dt.seconds)
        sprite.scaleX = extraScaleX
        sprite.scaleY = extraScaleY
        sprite.rotation = rotation
    }

    open fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        sprite.render(batch, camera, shapeRenderer)
    }

    open fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        sprite.debugRender(batch, camera, shapeRenderer)
    }

    private fun performSAT(poly2: List<Vec2f>): Boolean {
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
    fun isCollidingWith(from: GridEntity, useSat: Boolean = false): Boolean {
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

    fun isCollidingWithInnerCircle(from: GridEntity): Boolean =
        isCollidingWithRadius(innerRadius, from, from.innerRadius)

    fun isCollidingWithOuterCircle(from: GridEntity): Boolean =
        isCollidingWithRadius(outerRadius, from, from.outerRadius)

    fun isCollidingWithEncompassingCircle(from: GridEntity): Boolean =
        isCollidingWithRadius(encompassingRadius, from, from.encompassingRadius)

    fun isCollidingWithRadius(radius: Float, from: GridEntity, fromRadius: Float): Boolean {
        val dist = radius + fromRadius
        return distSqr(centerX, centerY, from.centerX, from.centerY) <= dist * dist
    }

    fun onPositionManuallyChanged() {
        lastPx = attachX
        lastPy = attachY
    }

    open fun updateGridPosition() {
        lastPx = attachX
        lastPy = attachY

        velocityX += calculateDeltaXGravity()
        velocityY += calculateDeltaYGravity()

        /**
         * Any movement greater than [maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val steps = ceil(abs(velocityX) + abs(velocityY) / maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                xr += velocityX / steps

                if (velocityX != 0f) {
                    preXCheck()
                    checkXCollision()
                }

                while (xr > 1) {
                    xr--
                    cx++
                }
                while (xr < 0) {
                    xr++
                    cx--
                }

                yr += velocityY / steps

                if (velocityY != 0f) {
                    preYCheck()
                    checkYCollision()
                }

                while (yr > 1) {
                    yr--
                    cy++
                }

                while (yr < 0) {
                    yr++
                    cy--
                }
                i++
            }
        }
        velocityX *= frictionX
        if (abs(velocityX) <= 0.0005f) {
            velocityX = 0f
        }

        velocityY *= frictionY
        if (abs(velocityY) <= 0.0005f) {
            velocityY = 0f
        }
    }

    open fun calculateDeltaXGravity(): Float {
        return 0f
    }

    open fun calculateDeltaYGravity(): Float {
        return 0f
    }

    open fun preXCheck() = Unit
    open fun preYCheck() = Unit

    open fun checkXCollision() = Unit
    open fun checkYCollision() = Unit

    open fun destroy() {
        destroyed = true
    }

    companion object {
        private val tempVec2f = MutableVec2f()
        private val tempVec2f2 = MutableVec2f()
        private val tempVec2f3 = MutableVec2f()
        private val tempVecList = MutableList(4) { MutableVec2f(0f) }
        private val tempVecList2 = MutableList(8) { MutableVec2f(0f) }
        private val tempVecList3 = MutableList(8) { MutableVec2f(0f) }
        private val tempFloatList = MutableList(4) { 0f }

    }
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