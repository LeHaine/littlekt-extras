package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.littlekt.graphics.g2d.Animation
import com.littlekt.graphics.g2d.AnimationPlayer
import com.littlekt.graphics.g2d.TextureSlice
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class AnimationComponent : Component<AnimationComponent> {
    private val player = AnimationPlayer<TextureSlice>()

    val totalFramesPlayed: Int get() = player.totalFramesPlayed
    val totalFrames: Int get() = player.totalFrames
    val currentFrameIdx: Int get() = player.currentFrameIdx
    val currentAnimation: Animation<TextureSlice>? get() = player.currentAnimation

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

    override fun type(): ComponentType<AnimationComponent> = AnimationComponent

    companion object : ComponentType<AnimationComponent>()
}