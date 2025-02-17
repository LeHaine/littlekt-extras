package com.lehaine.littlekt.extras.graphics

import com.littlekt.Context
import com.littlekt.graphics.Camera
import com.littlekt.math.MutableVec2f
import com.littlekt.math.nextPowerOfTwo
import kotlin.math.min

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class PixelSmoothRenderTarget private constructor(
    val width: Int,
    val height: Int,
    val pxWidth: Int,
    val pxHeight: Int
) {
    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu

    fun getWorldCoords(
        x: Int,
        y: Int,
        context: Context,
        camera: Camera,
        out: MutableVec2f
    ): MutableVec2f {
        out.x = (pxWidth / 100f) * ((100f / context.graphics.width) * x)
        out.y = (pxHeight / 100f) * ((100f / context.graphics.height) * y)
        out.x *= ppuInv
        out.y *= ppuInv
        out.x = out.x - width * ppuInv * 0.5f + camera.position.x
        out.y = out.y - height * ppuInv * 0.5f + camera.position.y
        return out
    }

    companion object {
        operator fun invoke(
            screenWidth: Int,
            screenHeight: Int,
            targetHeight: Int,
            maxWidth: Int = 0,
            maxHeight: Int = 0
        ): PixelSmoothRenderTarget {
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

            return PixelSmoothRenderTarget(
                pxWidth.nextPowerOfTwo,
                pxHeight.nextPowerOfTwo,
                pxWidth,
                pxHeight
            )
        }
    }
}