package com.lehaine.littlekt.extras.graphics

import com.littlekt.graphics.OrthographicCamera
import com.littlekt.math.MutableVec2f

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
open class PixelSmoothCamera : OrthographicCamera() {

    var ppu: Float = 1f
    val ppuInv: Float get() = 1f / ppu

    val offset = MutableVec2f()
    var renderTarget: PixelSmoothRenderTarget? = null
        set(value) {
            field = value
            if (value != null) {
                calculateOffset(value)
            }
        }

    private fun calculateOffset(fbo: PixelSmoothRenderTarget) {
        offset.set(((fbo.width - fbo.pxWidth) / 2).toFloat(), ((fbo.height - fbo.pxHeight) / 2).toFloat())
            .scale(ppuInv)
    }
}