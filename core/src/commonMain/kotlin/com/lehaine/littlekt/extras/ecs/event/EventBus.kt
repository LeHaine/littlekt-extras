package com.lehaine.littlekt.extras.ecs.event

import com.lehaine.littlekt.Disposable
import com.lehaine.littlekt.util.fastForEach
import kotlin.reflect.KClass


/**
 * @author Colton Daily
 * @date 3/9/2023
 */
class EventBus : Disposable {
    private val closeables = mutableListOf<Closeable>()
    private val perClassHandlers = HashMap<KClass<*>, MutableList<(Any) -> Unit>>()

    fun send(message: Any) {
        val clazz = message::class
        perClassHandlers[clazz]?.fastForEach { handler ->
            handler(message)
        }
    }

    private fun forClass(clazz: KClass<*>) = perClassHandlers.getOrPut(clazz) { mutableListOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: KClass<out T>, handler: (T) -> Unit): Closeable {
        val chandler = handler as ((Any) -> Unit)
        forClass(clazz).add(chandler)
        val closeable: Closeable = {
            forClass(clazz).remove(chandler)
        }
        closeables += closeable
        return closeable
    }

    inline fun <reified T : Any> register(noinline handler: (T) -> Unit): Closeable {
        return register(T::class, handler)
    }

    override fun dispose() {
        closeables.fastForEach { it.invoke() }
    }

}

typealias Closeable = () -> Unit