package com.lehaine.littlekt.extras

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene

/**
 * @author Colton Daily
 * @date 3/1/2023
 */
abstract class FixedScene(context: Context) : Scene(context) {

    open fun Context.fixedUpdate() = Unit
}