package com.lehaine.littlekt.extras

import com.littlekt.Context

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
abstract class FixedScene(context: Context) : Scene(context) {

    var fixedProgressionRatio: Float = 1f
    var tmod: Float = 1f

    open fun Context.fixedUpdate() = Unit
}