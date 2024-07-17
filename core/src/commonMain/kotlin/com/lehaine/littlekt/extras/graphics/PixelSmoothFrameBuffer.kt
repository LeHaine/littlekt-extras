package com.lehaine.littlekt.extras.graphics

import com.littlekt.Context
import com.littlekt.Releasable
import com.littlekt.graphics.Camera
import com.littlekt.graphics.EmptyTexture
import com.littlekt.graphics.webgpu.Device
import com.littlekt.graphics.webgpu.TextureFormat
import com.littlekt.math.MutableVec2f
import com.littlekt.math.nextPowerOfTwo
import kotlin.math.min

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
class PixelSmoothFrameBuffer private constructor(
    device: Device,
    format: TextureFormat,
    val width: Int,
    val height: Int,
    val pxWidth: Int,
    val pxHeight: Int
) : Releasable {
    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu
    val target = EmptyTexture(device, format, width, height)

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
        out.x = out.x - target.width * ppuInv * 0.5f + camera.position.x
        out.y = out.y - target.height * ppuInv * 0.5f + camera.position.y
        return out
    }

    override fun release() {
        target.release()
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