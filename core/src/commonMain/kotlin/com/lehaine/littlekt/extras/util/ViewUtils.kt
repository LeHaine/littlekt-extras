package com.lehaine.littlekt.extras.util

import com.littlekt.math.Rect


fun Rect.intersects(tx: Float, ty: Float): Boolean =
    tx >= x && tx <= x2 && ty >= y && ty <= y2

/**
 * @param cellPad padding in cells. e.g. a value of `2` will be equivalent of `gridCellSize * 2` in pixels.`
 */
fun Rect.intersectsGrid(cx: Float, cy: Float, gridCellSize: Int, cellPad: Int = 2): Boolean {
    val paddingPx = gridCellSize * cellPad
    return cx * gridCellSize >= x - paddingPx && (cx + 1) * gridCellSize <= x2 + paddingPx
            && cy * gridCellSize >= y - paddingPx && (cy + 1) * gridCellSize <= y2 - paddingPx
}