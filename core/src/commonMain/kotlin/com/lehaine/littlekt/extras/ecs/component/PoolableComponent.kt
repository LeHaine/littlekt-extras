package com.lehaine.littlekt.extras.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.InjectableConfiguration
import com.github.quillraven.fleks.World
import com.littlekt.util.datastructure.Pool

/**
 * @author Colton Daily
 * @date 12/18/2024
 */
interface PoolableComponent<T> : Component<T> {
    val poolType: PoolType<T>

    fun reset()

    override fun World.onRemove(entity: Entity) {
        runCatching {
            val pool = inject<Pool<T>>(poolType.poolName)
            @Suppress("UNCHECKED_CAST")
            pool.free(this@PoolableComponent as T)
        }
    }
}

interface PoolType<T> {
    val poolName: String

    fun alloc(world: World): T {
        val pool = world.inject<Pool<T>>(poolName)
        return pool.alloc()
    }
}

inline fun <reified T> poolTypeOf(
    typeName: String = T::class.simpleName ?: T::class.toString(),
): PoolType<T> = object : PoolType<T> {
    override val poolName: String = "${typeName}Pool"
}

fun <T : PoolableComponent<T>> InjectableConfiguration.addPool(
    poolType: PoolType<T>,
    preallocate: Int = 0,
    gen: (Int) -> T
) {
    val pool = Pool(reset = { it.reset() }, preallocate, gen)
    add(poolType.poolName, pool)
}