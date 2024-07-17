package com.lehaine.littlekt.extras

import com.littlekt.Context
import com.littlekt.ContextListener
import com.littlekt.RemoveContextCallback
import com.littlekt.util.datastructure.fastForEach
import com.littlekt.util.milliseconds
import com.littlekt.util.seconds
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 2/28/2023
 */
abstract class FixedTimeContextListener(context: Context) : ContextListener(context) {
    /**
     * The target FPS for [tmod].
     */
    var targetFPS = 60

    /**
     * The time modifier based off of [targetFPS].
     *
     * If [targetFPS] is set to `60` and the application is running at `120` FPS then this value will be `0.5f`
     * This can be used instead of [dt] to handle frame independent logic.
     */
    var tmod: Float = 1f
        private set

    /**
     * The fixed progression lerp ratio for fixed updates. This is used for handling updating items that
     * use [onFixedUpdate] for movement / physics logic.
     */
    var fixedProgressionRatio: Float = 1f

    /**
     * The interval for [onFixedUpdate] to fire. Defaults to `30` times per second.
     */
    var fixedTimesPerSecond: Int = 30
        set(value) {
            field = value
            time = (1f / value).seconds
        }
    private var time = (1f / fixedTimesPerSecond).seconds
    private var accum: Duration = Duration.ZERO

    protected val fixedUpdateCalls = mutableListOf<() -> Unit>()
    protected val removeEntityOnRender: RemoveContextCallback

    init {
        removeEntityOnRender = context.onUpdate(::updateFixedTimes)
    }

    /**
     * Ensure [removeEntityOnRender] is invoked before manually updating the fixed times.
     */
    protected fun updateFixedTimes(dt: Duration) {
        tmod = dt.seconds * targetFPS

        accum += dt
        while (accum >= time) {
            accum -= time
            fixedUpdateCalls.fastForEach {
                it.invoke()
            }
        }
        fixedProgressionRatio = accum.milliseconds / time.milliseconds
    }

    fun onFixedUpdate(action: () -> Unit): RemoveContextCallback {
        fixedUpdateCalls += action
        return {
            check(fixedUpdateCalls.contains(action)) { "the 'onFixedUpdate' action has already been removed!" }
            fixedUpdateCalls -= action
        }
    }
}