package com.lehaine.littlekt.extras.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.Interval
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.lehaine.littlekt.extras.ecs.component.Grid
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollision
import com.lehaine.littlekt.extras.ecs.component.GridEntityCollisionResult
import com.littlekt.math.MutableVec2f
import com.littlekt.math.Vec2f
import com.littlekt.math.distSqr
import com.littlekt.math.geom.radians

/**
 * @author Colton Daily
 * @date 12/22/2024
 */
class GridEntityCollisionSystem(
    interval: Interval = Fixed(1 / 30f)
) : IteratingSystem(family = family { all(Grid, GridEntityCollision) }, interval = interval) {

    override fun onTickEntity(entity: Entity) {
        val grid = entity[Grid]
        val useSat = entity[GridEntityCollision].useSat

        family.forEach { targetEntity ->
            if (targetEntity == entity) return@forEach
            val targetGrid = targetEntity[Grid]
            // if we aren't colliding with outer circle then we aren't colliding with inner or aabb
            if (grid.isCollidingWithOuterCircle(targetGrid)) {
                entity.addOuterCollision(targetGrid)
                targetEntity.addOuterCollision(grid)

                if (grid.isCollidingWithInnerCircle(targetGrid)) {
                    entity.addInnerCollision(targetGrid)
                    targetEntity.addInnerCollision(grid)
                }
                if (grid.isCollidingWith(targetGrid, useSat)) {
                    entity.addAABBCollision(targetGrid)
                    targetEntity.addAABBCollision(grid)
                }
            }
        }
    }

    private fun Entity.addAABBCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.AABBCollision)
            ?: GridEntityCollisionResult.AABBCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }

    private fun Entity.addOuterCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.OuterCircleCollision)
            ?: GridEntityCollisionResult.OuterCircleCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }

    private fun Entity.addInnerCollision(target: Grid) {
        val aabbCollision = getOrNull(GridEntityCollisionResult.InnerCircleCollision)
            ?: GridEntityCollisionResult.InnerCircleCollisionPool.alloc(world)
        aabbCollision.collidingWith += target
    }

    private fun Grid.performSAT(poly2: List<Vec2f>): Boolean {
        val edges = tempVecList2
        var i = 0
        polyToEdges(vertices).forEach {
            edges[i].set(it)
            i++
        }

        polyToEdges(poly2).forEach {
            edges[i].set(it)
            i++
        }
        val axes = tempVecList3

        repeat(edges.size) { index ->
            axes[index].set(orthogonal(edges[index]))
        }

        for (axis in axes) {
            val projection1 = tempVec2f2.set(project(vertices, axis))
            val projection2 = tempVec2f3.set(project(poly2, axis))
            if (!overlap(projection1, projection2)) {
                return false
            }
        }

        return true
    }

    private fun edgeVector(v1: Vec2f, v2: Vec2f): Vec2f = tempVec2f.set(v2).subtract(v1)

    private fun polyToEdges(poly: List<Vec2f>): List<Vec2f> {
        repeat(poly.size) { index ->
            tempVecList[index].set(edgeVector(poly[index], poly[(index + 1) % poly.size]))
        }
        return tempVecList
    }

    private fun orthogonal(vec2f: Vec2f): Vec2f = tempVec2f.set(vec2f.y, -vec2f.x)

    private fun project(poly: List<Vec2f>, axis: Vec2f): Vec2f {
        repeat(poly.size) { index ->
            tempFloatList[index] = poly[index].dot(axis)
        }
        return tempVec2f.set(tempFloatList.min(), tempFloatList.max())
    }

    private fun overlap(projection1: Vec2f, projection2: Vec2f) =
        projection1.x <= projection2.y && projection2.x <= projection1.y

    /**
     * AABB check
     */
    private fun Grid.isCollidingWith(from: Grid, useSat: Boolean = false): Boolean {
        if (useSat) {
            if (rotation != 0.radians || from.rotation != 0.radians) {
                if (!isCollidingWithEncompassingCircle(from)) return false
                return performSAT(from.vertices)
            }
        }

        // normal rectangle overlap check
        val lx = left
        val lx2 = from.left
        val rx = right
        val rx2 = from.right

        if (lx >= rx2 || lx2 >= rx) {
            return false
        }

        val ly = bottom
        val ry = top
        val ly2 = from.bottom
        val ry2 = from.top

        return !(ly >= ry2 || ly2 >= ry)
    }

    private fun Grid.isCollidingWithInnerCircle(from: Grid): Boolean =
        isCollidingWithRadius(innerRadius, from, from.innerRadius)

    private fun Grid.isCollidingWithOuterCircle(from: Grid): Boolean =
        isCollidingWithRadius(outerRadius, from, from.outerRadius)

    private fun Grid.isCollidingWithEncompassingCircle(from: Grid): Boolean =
        isCollidingWithRadius(encompassingRadius, from, from.encompassingRadius)

    private fun Grid.isCollidingWithRadius(radius: Float, from: Grid, fromRadius: Float): Boolean {
        val dist = radius + fromRadius
        return distSqr(centerX, centerY, from.centerX, from.centerY) <= dist * dist
    }

    companion object {
        private val tempVec2f = MutableVec2f()
        private val tempVec2f2 = MutableVec2f()
        private val tempVec2f3 = MutableVec2f()
        private val tempVecList = MutableList(4) { MutableVec2f(0f) }
        private val tempVecList2 = MutableList(8) { MutableVec2f(0f) }
        private val tempVecList3 = MutableList(8) { MutableVec2f(0f) }
        private val tempFloatList = MutableList(4) { 0f }
    }
}