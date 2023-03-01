package com.lehaine.littlekt.extras.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.g2d.Animation
import com.lehaine.littlekt.graphics.g2d.AnimationPlayer
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.util.SingleSignal
import com.lehaine.littlekt.util.signal1v
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class AnimatedSprite : Sprite() {

    private val player = AnimationPlayer<TextureSlice>().apply {
        onFrameChange = ::handleFrameChange
    }

    val onFrameChanged: SingleSignal<Int> = signal1v()

    val totalFramesPlayed: Int get() = player.totalFramesPlayed
    val totalFrames: Int get() = player.totalFrames
    val currentFrameIdx: Int get() = player.currentFrameIdx

    fun update(dt: Duration) {
        player.update(dt)
    }

    fun play(anim: Animation<TextureSlice>, times: Int = 1, queue: Boolean = false) = player.play(anim, times, queue)

    fun play(frame: TextureSlice, frameTime: Duration = 50.milliseconds, numFrames: Int = 1) =
        player.play(frame, frameTime, numFrames)

    fun play(animation: Animation<TextureSlice>, duration: Duration, queue: Boolean = false) =
        player.play(animation, duration, queue)

    fun playLooped(animation: Animation<TextureSlice>) = player.playLooped(animation)

    fun playOnce(animation: Animation<TextureSlice>) = player.playOnce(animation)

    fun registerState(
        anim: Animation<TextureSlice>,
        priority: Int,
        loop: Boolean = true,
        reason: () -> Boolean = { true },
    ) = player.registerState(anim, priority, loop, reason)

    fun removeState(anim: Animation<TextureSlice>) = player.removeState(anim)

    fun removeAllStates() = player.removeAllStates()

    fun resume() = player.resume()

    fun start() = player.start()

    fun restart() = player.restart()

    fun stop() = player.stop()

    private fun handleFrameChange(frame: Int) {
        slice = player.currentAnimation?.getFrame(frame) ?: slice
        onFrameChanged.emit(frame)
    }

    fun destroy() {
        onFrameChanged.clear()
    }
}