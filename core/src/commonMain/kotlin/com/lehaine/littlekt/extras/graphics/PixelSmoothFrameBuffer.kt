package com.lehaine.littlekt.extras.graphics

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.FrameBuffer
import com.lehaine.littlekt.graphics.gl.TexMagFilter
import com.lehaine.littlekt.graphics.gl.TexMinFilter
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.nextPowerOfTwo
import kotlin.math.min

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class PixelSmoothFrameBuffer private constructor(
    width: Int,
    height: Int,
    val pxWidth: Int,
    val pxHeight: Int
) : FrameBuffer(width, height, minFilter = TexMinFilter.NEAREST, magFilter = TexMagFilter.NEAREST) {

    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu

    fun getWorldCoords(x: Int, y: Int, context: Context, camera: Camera, out: MutableVec2f) {
        out.x = (pxWidth / 100f) * ((100f / context.graphics.width) * x)
        out.y = (pxHeight / 100f) * ((100f / context.graphics.height) * y)
        out.x *= ppuInv
        out.y *= ppuInv
        out.x = out.x - width * ppuInv * 0.5f + camera.position.x
        out.y = out.y - height * ppuInv * 0.5f + camera.position.y
    }

    companion object {
        operator fun invoke(
            screenWidth: Int,
            screenHeight: Int,
            targetHeight: Int,
            maxWidth: Int = 0,
            maxHeight: Int = 0
        ): PixelSmoothFrameBuffer {
            var h = screenHeight / (screenHeight / targetHeight)
            var w = (screenWidth / (screenHeight / h))
            if (maxWidth > 0) {
                w = min(w, maxWidth)
            }
            if (maxHeight > 0) {
                h = min(h, maxHeight)
            }
            val pxWidth = w
            val pxHeight = h

            return PixelSmoothFrameBuffer(pxWidth.nextPowerOfTwo, pxHeight.nextPowerOfTwo, pxWidth, pxHeight)
        }
    }
}