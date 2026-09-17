package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceSolidTest {
    @Test
    fun `each die is a closed convex solid with unique fixed face numbers`() {
        for (type in DiceType.entries) {
            val solid = DiceSolids.forType(type)
            assertEquals((1..type.sides).toList(), solid.faces.map { it.value })
            val edges = mutableMapOf<Pair<Int, Int>, Int>()
            for (face in solid.faces) {
                assertEquals(1.0, face.normal.length(), 0.000001)
                assertEquals(0.0, face.normal.dot(face.up), 0.000001)
                assertEquals(0.0, face.normal.dot(face.right), 0.000001)
                assertTrue(face.labelRadius > 0)
                for (vertex in solid.vertices) {
                    assertTrue((vertex - face.center).dot(face.normal) < 0.000001)
                }
                for (i in face.vertices.indices) {
                    val a = face.vertices[i]
                    val b = face.vertices[(i + 1) % face.vertices.size]
                    assertEquals(0.0, (solid.vertices[a] - face.center).dot(face.normal), 0.000001)
                    val edge = minOf(a, b) to maxOf(a, b)
                    edges[edge] = (edges[edge] ?: 0) + 1
                }
            }
            assertTrue(edges.values.all { it == 2 })
            assertEquals(2, solid.vertices.size - edges.size + solid.faces.size)
            assertTrue(solid.faces.all { it.vertices.size == if (type == DiceType.D8) 3 else 4 })
        }
    }

    @Test
    fun `opposite face numbers sum to the number of sides plus one`() {
        for (type in DiceType.entries) {
            val solid = DiceSolids.forType(type)
            for (face in solid.faces) {
                val opposite = solid.faces.single { it.value == type.sides + 1 - face.value }
                assertEquals(-1.0, face.normal.dot(opposite.normal), 0.000001)
            }
        }
    }

    @Test
    fun `every result rests on the most front facing face with visible numbered sides`() {
        val matrix = DoubleArray(9)
        for (type in DiceType.entries) {
            val solid = DiceSolids.forType(type)
            for (result in 1..type.sides) {
                solid.restingPoses[result - 1].writeMatrix(matrix)
                fun depth(vector: DiceVector) = matrix[6] * vector.x + matrix[7] * vector.y + matrix[8] * vector.z
                val visible = solid.faces.filter { depth(it.normal) > 0.07 }
                assertTrue(visible.size >= 2)
                assertEquals(result, visible.maxBy { depth(it.normal) }.value)
                val resultFace = solid.faces[result - 1]
                val upright = matrix[3] * resultFace.up.x + matrix[4] * resultFace.up.y + matrix[5] * resultFace.up.z
                assertTrue(upright > 0.8)
            }
        }
    }

    @Test
    fun `the whole roll slows monotonically without a second landing turn`() {
        for (type in DiceType.entries) {
            val solid = DiceSolids.forType(type)
            for (index in 0..19) {
                for (result in 1..type.sides) {
                    val scratch = DiceQuaternion()
                    var previous = DiceQuaternion()
                    setDiceOrientation(previous, scratch, solid, 0f, result, index)
                    var previousStepAngle = Double.POSITIVE_INFINITY
                    for (step in 1..100) {
                        val current = DiceQuaternion()
                        setDiceOrientation(current, scratch, solid, step / 100f, result, index)
                        val dot = current.x * previous.x + current.y * previous.y +
                            current.z * previous.z + current.w * previous.w
                        val angle = 2 * kotlin.math.acos(kotlin.math.abs(dot).coerceIn(0.0, 1.0))
                        assertTrue("Acceleration at frame $step", angle <= previousStepAngle + 0.00001)
                        previousStepAngle = angle
                        previous = current
                    }
                    val target = solid.restingPoses[result - 1]
                    val agreement = previous.x * target.x + previous.y * target.y +
                        previous.z * target.z + previous.w * target.w
                    assertEquals(1.0, kotlin.math.abs(agreement), 0.000001)
                    assertTrue(previousStepAngle < 0.002)
                }
            }
        }
    }

    @Test
    fun `fixed numbers remain attached to the same mesh faces throughout a roll`() {
        for (type in DiceType.entries) {
            val solid = DiceSolids.forType(type)
            val numbers = solid.faces.map { it.value }
            val scratch = DiceQuaternion()
            val pose = DiceQuaternion()
            for (step in 0..100) {
                setDiceOrientation(pose, scratch, solid, step / 100f, type.sides, 0)
                assertEquals(numbers, solid.faces.map { it.value })
                assertEquals(1.0, pose.x * pose.x + pose.y * pose.y + pose.z * pose.z + pose.w * pose.w, 0.000001)
            }
        }
    }
}
