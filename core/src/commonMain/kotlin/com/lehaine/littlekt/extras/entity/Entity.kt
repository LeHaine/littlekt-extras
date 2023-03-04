package com.lehaine.littlekt.extras.entity

import com.lehaine.littlekt.extras.Cooldown
import com.lehaine.littlekt.extras.renderable.AnimatedSprite
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.math.distSqr
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.cosine
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.geom.sine
import com.lehaine.littlekt.math.interpolate
import com.lehaine.littlekt.util.seconds
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
fun entity(gridCellSize: Float, callback: @EntityDslMarker Entity.() -> Unit = {}): Entity {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return Entity(gridCellSize).also(callback)
}

open class Entity(val gridCellSize: Float) {
    val sprite = AnimatedSprite()

    var anchorX: Float
        get() = sprite.anchorX
        set(value) {
            sprite.anchorX = value
        }
    var anchorY: Float
        get() = sprite.anchorY
        set(value) {
            sprite.anchorY = value
        }

    var cx: Int = 0
    var cy: Int = 0
    var xr: Float = 0.5f
    var yr: Float = 1f

    var gravityX: Float = 0f
    var gravityY: Float = 0f
    var gravityMultiplier: Float = 1f
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var frictionX: Float = 0.82f
    var frictionY: Float = 0.82f
    var maxGridMovementPercent: Float = 0.33f

    var width: Float = gridCellSize
    var height: Float = gridCellSize

    val innerRadius get() = min(width, height) * ppuInv * 0.5
    val outerRadius get() = max(width, height) * ppuInv * 0.5

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
     * The current entity x-scaling.
     */
    var entityScaleX = 1f

    /**
     * The current entity y-scaling.
     */
    var entityScaleY = 1f

    var restoreSpeed: Float = 12f

    var dir: Int = 1

    var ppu: Float = 1f
        set(value) {
            field = value
            sprite.ppu = value
        }

    val ppuInv: Float get() = 1f / ppu

    var fixedProgressionRatio = 1f

    val x: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }

    val y: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPy, attachY)
            } else {
                attachY
            }
        }

    var scaleX: Float = 1f
    var scaleY: Float = 1f
    var rotation: Angle = Angle.ZERO

    open val attachX get() = ((cx + xr) * gridCellSize) * ppuInv
    open val attachY get() = ((cy + yr) * gridCellSize) * ppuInv
    val centerX get() = attachX + (0.5f - anchorX) * width
    val centerY get() = attachY + (0.5f - anchorY) * height
    val top get() = attachY - anchorY * height * ppuInv
    val right get() = attachX + (1 - anchorX) * width * ppuInv
    val bottom get() = attachY + (1 - anchorY) * height * ppuInv
    val left get() = attachX - anchorX * width * ppuInv

    private val _topLeft = MutableVec2f()
    val topLeft: Vec2f
        get() = _topLeft.set(left, top).calculateVertex(centerX, centerY, rotation)

    private val _bottomLeft = MutableVec2f()
    val bottomLeft: Vec2f
        get() = _bottomLeft.set(left, bottom).calculateVertex(centerX, centerY, rotation)

    private val _bottomRight = MutableVec2f()
    val bottomRight: Vec2f
        get() = _bottomRight.set(right, bottom).calculateVertex(centerX, centerY, rotation)

    private val _topRight = MutableVec2f()
    private val topRight: Vec2f
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

//    val mouseX get() = (canvas as? PixelSmoothFrameBuffer)?.mouseX ?: 0f
//    val mouseY get() = (canvas as? PixelSmoothFrameBuffer)?.mouseY ?: 0f
//    val angleToMouse: Angle
//        get() = atan2(
//            mouseY - centerY,
//            mouseX - centerX
//        ).radians
//
//    val dirToMouse: Int get() = dirTo(mouseX)

    private var ignorePosChanged = false

    init {
        anchorX = 0.5f
        anchorY = 1f

        updateGridPosition()
    }

    open fun preUpdate(dt: Duration) {
        cd.update(dt)
    }

    open fun fixedUpdate() {
        updateGridPosition()
    }

    open fun update(dt: Duration) = Unit

    open fun postUpdate(dt: Duration) {
        ignorePosChanged = true
        sprite.x = x
        sprite.y = y
        ignorePosChanged = false
        entityScaleX = scaleX * dir * stretchX
        entityScaleY = scaleY * stretchY
        _stretchX += (1 - _stretchX) * min(1f, restoreSpeed * dt.seconds)
        _stretchY += (1 - _stretchY) * min(1f, restoreSpeed * dt.seconds)
        sprite.scaleX = entityScaleX
        sprite.scaleY = entityScaleY
        sprite.rotation = rotation
    }

    open fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        sprite.render(batch, camera, shapeRenderer)
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
    fun isCollidingWith(from: Entity, useSat: Boolean = false): Boolean {
        if (useSat) {
            if (rotation != 0.radians || from.rotation != 0.radians) {
                if (!isCollidingWithOuterCircle(from)) return false
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

        val ly = top
        val ry = bottom
        val ly2 = from.top
        val ry2 = from.bottom

        if (ly >= ry2 || ly2 >= ry) {
            return false
        }

        return true
    }

    fun isCollidingWithInnerCircle(from: Entity): Boolean {
        val dist = innerRadius + from.innerRadius
        return distSqr(centerX, centerY, from.centerX, from.centerY) <= dist * dist
    }

    fun isCollidingWithOuterCircle(from: Entity): Boolean {
        val dist = outerRadius + from.outerRadius
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