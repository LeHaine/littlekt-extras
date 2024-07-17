package com.lehaine.littlekt.extras.renderable

import com.littlekt.graphics.Camera
import com.littlekt.graphics.Color
import com.littlekt.graphics.g2d.Batch
import com.littlekt.graphics.g2d.shape.ShapeRenderer
import com.littlekt.graphics.webgpu.BlendState
import com.littlekt.math.Mat3
import com.littlekt.math.MutableVec2f
import com.littlekt.math.Rect
import com.littlekt.math.Vec2f
import com.littlekt.math.geom.Angle
import com.littlekt.math.geom.normalized

abstract class Renderable2D {

    /**
     * The width of the [Renderable2D].
     */
    abstract val renderWidth: Float

    /**
     * The height of the [Renderable2D].
     */
    abstract val renderHeight: Float

    var blendMode: BlendState? = BlendState.NonPreMultiplied

    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu

    var position: MutableVec2f = MutableVec2f()
        set(value) {
            field = value
            boundsDirty = true
        }
    var x: Float
        get() = position.x
        set(value) {
            boundsDirty = true
            position.x = value
        }
    var y: Float
        get() = position.y
        set(value) {
            boundsDirty = true
            position.y = value
        }

    var scale: MutableVec2f = MutableVec2f(1f, 1f)
        set(value) {
            field = value
            boundsDirty = true
        }
    var scaleX: Float
        get() = scale.x
        set(value) {
            boundsDirty = true
            scale.x = value
        }
    var scaleY: Float
        get() = scale.y
        set(value) {
            boundsDirty = true
            scale.y = value
        }

    var rotation: Angle = Angle.ZERO
        set(value) {
            field = value
            boundsDirty = true
        }

    /**
     * The AABB that wraps this [Renderable2D]. Used for camera culling.
     */
    open val renderBounds: Rect
        get() {
            if (boundsDirty) {
                calculateBounds(
                    position = position,
                    renderWidth * anchorX,
                    renderHeight * anchorY,
                    scale = scale,
                    rotation = rotation,
                    width = renderWidth,
                    height = renderHeight
                )
                boundsDirty = false
            }
            return _bounds
        }

    val anchor: Vec2f get() = _anchor

    var anchorX: Float
        get() {
            return anchor.x
        }
        set(value) {
            if (value == _anchor.x) {
                return
            }
            _anchor.x = value
            boundsDirty = true
        }

    var anchorY: Float
        get() {
            return _anchor.y
        }
        set(value) {
            if (value == _anchor.y) {
                return
            }
            _anchor.y = value
            boundsDirty = true
        }

    val localOffset: Vec2f get() = _localOffset

    var localOffsetX: Float
        get() {
            return _localOffset.x
        }
        set(value) {
            if (value == _localOffset.x) {
                return
            }
            _localOffset.x = value
            boundsDirty = true
        }

    var localOffsetY: Float
        get() {
            return _localOffset.y
        }
        set(value) {
            if (value == _localOffset.y) {
                return
            }
            _localOffset.y = value
            boundsDirty = true
        }

    /**
     * Color that is passed along to the [Batch].
     */
    var color = Color.WHITE.toMutableColor()

    private val transMat = Mat3()
    private val tempMat = Mat3()

    private val topLeft = MutableVec2f()
    private val topRight = MutableVec2f()
    private val bottomLeft = MutableVec2f()
    private val bottomRight = MutableVec2f()

    protected val _anchor = MutableVec2f()
    protected val _localOffset = MutableVec2f()

    protected var _bounds = Rect()
    protected var boundsDirty = true

    open fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) = Unit

    open fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        shapeRenderer.rectangle(renderBounds, color = Color.YELLOW)
    }

    protected fun calculateBounds(
        position: Vec2f,
        originX: Float,
        originY: Float,
        scale: Vec2f,
        rotation: Angle,
        width: Float,
        height: Float
    ) {
        if (rotation.normalized == Angle.ZERO) {
            _bounds.let {
                it.x = position.x - originX * scale.x
                it.y = position.y - originY * scale.y
                it.width = width * scale.x
                it.height = height * scale.y
            }
        } else {
            val worldPosX = position.x
            val worldPosY = position.y

            transMat.setToTranslate(-worldPosX - originX, -worldPosY - originY)
            tempMat.setToScale(scale.x, scale.y) // scale
            transMat.mulLeft(tempMat)
            tempMat.setToRotation(rotation) // rotate
            transMat.mulLeft(tempMat)
            tempMat.setToTranslate(worldPosX, worldPosY) // translate back
            transMat.mulLeft(tempMat)

            // get four corners
            bottomLeft.set(worldPosX, worldPosY)
            bottomRight.set(worldPosX + width, worldPosY)
            topLeft.set(worldPosX, worldPosY + height)
            topRight.set(worldPosX + width, worldPosY + height)

            bottomLeft.mul(transMat)
            bottomRight.mul(transMat)
            topLeft.mul(transMat)
            topRight.mul(transMat)

            val minX = minOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val maxX = maxOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val minY = minOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)
            val maxY = maxOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)

            _bounds.set(minX, minY, maxX - minX, maxY - minY)
        }
    }
}