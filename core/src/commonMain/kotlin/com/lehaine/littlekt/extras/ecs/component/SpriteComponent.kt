package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.lehaine.littlekt.extras.renderable.Sprite
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice

/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class SpriteComponent(slice: TextureSlice? = null) : Component<SpriteComponent> {
    var layer: Int = 0

    var slice: TextureSlice? = slice
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    val renderWidth: Float
        get() = if (overrideWidth) overriddenWidth else slice?.width?.toFloat() ?: 0f

    val renderHeight: Float
        get() = if (overrideHeight) overriddenHeight else slice?.height?.toFloat() ?: 0f

    var overrideWidth = false
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var overrideHeight = false
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }

    var overriddenWidth = 0f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }
    var overriddenHeight = 0f
        set(value) {
            if (field == value) return
            field = value
            dirty = true
        }

    internal var dirty = false

    /**
     * Flips the current rendering of the [Sprite] horizontally.
     */
    var flipX = false

    /**
     * Flips the current rendering of the [Sprite] vertically.
     */
    var flipY = false

    /**
     * Color that is passed along to the [Batch].
     */
    var color = Color.WHITE.toMutableColor()

    override fun type(): ComponentType<SpriteComponent> = SpriteComponent

    companion object : ComponentType<SpriteComponent>()

}