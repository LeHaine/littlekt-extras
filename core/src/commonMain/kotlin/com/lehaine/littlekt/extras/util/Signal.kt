package com.lehaine.littlekt.extras.util

import com.littlekt.util.datastructure.fastForEach

class SingleSignal<T> {
    private val connections = mutableListOf<(T) -> Unit>()

    /** Emit this signal to all connections and connected nodes. */
    fun emit(value: T) {
        connections.fastForEach { it(value) }
    }

    operator fun plusAssign(slot: (T) -> Unit) {
        connections += slot
    }

    operator fun minusAssign(slot: (T) -> Unit) {
        connections -= slot
    }

    /** Clear all connections. */
    fun clear() {
        connections.clear()
    }
}

fun <T> signal1v(): SingleSignal<T> = SingleSignal()