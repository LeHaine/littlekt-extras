package com.lehaine.littlekt.extras.graphics

import com.lehaine.littlekt.graphics.OrthographicCamera
import com.lehaine.littlekt.math.MutableVec2f

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
open class PixelSmoothCamera : OrthographicCamera() {

    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu

    val offset = MutableVec2f()
    var fbo: PixelSmoothFrameBuffer? = null
        set(value) {
            field = value
            if (value != null) {
                calculateOffset(value)
            }
        }

    private fun calculateOffset(fbo: PixelSmoothFrameBuffer) {
        offset.set(((fbo.width - fbo.pxWidth) / 2).toFloat(), ((fbo.height - fbo.pxHeight) / 2).toFloat())
            .scale(ppuInv)
    }
}