package com.lehaine.littlekt.extras

import kotlin.jvm.JvmInline

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
@JvmInline
value class GridPoint internal constructor(@PublishedApi internal val packedValue: Long) : Comparable<GridPoint> {

    val cx: Int get() = unpackInt(packedValue, 0)
    val cy: Int get() = unpackInt(packedValue, 1)
    val width: Int get() = unpackInt(packedValue, 2)
    val coordId: Int get() = cx + cy * width

    override fun compareTo(other: GridPoint): Int = coordId.compareTo(other.coordId)

    override fun toString(): String {
        return "GridPoint(cx=$cx, cy=$cy, width=$width)"
    }

    companion object {
        operator fun invoke(cx: Int, cy: Int, gridWidth: Int) =
            GridPoint(packInts(cx, cy, gridWidth))

        private fun packInts(a: Int, b: Int, c: Int): Long {
            return (a.toLong() shl 32) or (b.toLong() shl 16) or c.toLong()
        }

        private fun unpackInt(value: Long, idx: Int): Int {
            return when (idx) {
                0 -> {
                    (value shr 32) and 0xFFFFL
                }

                1 -> {
                    (value shr 16) and 0xFFFFL
                }

                else -> {
                    value and 0xFFFFL
                }
            }.toInt()
        }
    }
}

