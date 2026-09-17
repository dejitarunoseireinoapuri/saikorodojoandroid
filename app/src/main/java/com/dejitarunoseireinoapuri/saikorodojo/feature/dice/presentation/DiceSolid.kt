package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class DiceVector(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: DiceVector) = DiceVector(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: DiceVector) = DiceVector(x - other.x, y - other.y, z - other.z)
    operator fun times(scale: Double) = DiceVector(x * scale, y * scale, z * scale)
    fun dot(other: DiceVector) = x * other.x + y * other.y + z * other.z
    fun cross(other: DiceVector) = DiceVector(
        y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x
    )
    fun length() = sqrt(dot(this))
    fun normalized() = this * (1.0 / length())
}

internal class DiceSolidFace(
    val value: Int,
    val vertices: IntArray,
    val center: DiceVector,
    val normal: DiceVector,
    val right: DiceVector,
    val up: DiceVector,
    val labelRadius: Double
)

/** Face numbers and their local lettering axes never change during a roll. */
internal class DiceSolid(val vertices: List<DiceVector>, numberedFaces: List<Pair<Int, IntArray>>) {
    val faces = numberedFaces.map { (number, indices) ->
        val center = indices.fold(DiceVector(0.0, 0.0, 0.0)) { sum, i -> sum + vertices[i] } *
            (1.0 / indices.size)
        var normal = (vertices[indices[1]] - vertices[indices[0]])
            .cross(vertices[indices[2]] - vertices[indices[0]]).normalized()
        val winding = if (normal.dot(center) < 0) {
            normal = normal * -1.0
            indices.reversedArray()
        } else indices
        val vertical = if (abs(normal.y) < 0.95) DiceVector(0.0, 1.0, 0.0) else DiceVector(0.0, 0.0, 1.0)
        val right = vertical.cross(normal).normalized()
        val up = normal.cross(right)
        val radius = winding.indices.minOf { i ->
            val a = vertices[winding[i]]
            val edge = vertices[winding[(i + 1) % winding.size]] - a
            (center - a).cross(edge).length() / edge.length()
        }
        DiceSolidFace(number, winding, center, normal, right, up, radius)
    }.sortedBy { it.value }
    val radius = vertices.maxOf { it.length() }

    val restingPoses: List<DiceQuaternion> = faces.map { face ->
        val facing = DiceQuaternion().setBasis(face.right, face.up, face.normal)
        val tilt = DiceQuaternion().setEuler(-0.28, -0.34, 0.025)
        DiceQuaternion().setProduct(tilt, facing)
    }
}

internal object DiceSolids {
    private val cube by lazy {
        DiceSolid(
            listOf(
                DiceVector(-1.0, -1.0, -1.0), DiceVector(1.0, -1.0, -1.0),
                DiceVector(1.0, 1.0, -1.0), DiceVector(-1.0, 1.0, -1.0),
                DiceVector(-1.0, -1.0, 1.0), DiceVector(1.0, -1.0, 1.0),
                DiceVector(1.0, 1.0, 1.0), DiceVector(-1.0, 1.0, 1.0)
            ),
            listOf(
                1 to intArrayOf(4, 5, 6, 7), 6 to intArrayOf(0, 3, 2, 1),
                2 to intArrayOf(1, 2, 6, 5), 5 to intArrayOf(0, 4, 7, 3),
                3 to intArrayOf(3, 7, 6, 2), 4 to intArrayOf(0, 1, 5, 4)
            )
        )
    }
    private val octahedron by lazy {
        val vertices = listOf(
            DiceVector(1.0, 0.0, 0.0), DiceVector(-1.0, 0.0, 0.0),
            DiceVector(0.0, 1.0, 0.0), DiceVector(0.0, -1.0, 0.0),
            DiceVector(0.0, 0.0, 1.0), DiceVector(0.0, 0.0, -1.0)
        )
        DiceSolid(vertices, (0..7).map { bits ->
            (bits + 1) to intArrayOf(bits and 1, 2 + ((bits shr 1) and 1), 4 + ((bits shr 2) and 1))
        })
    }
    private val trapezohedron by lazy {
        val poleHeight = 1.05
        // This ratio makes each four-vertex kite exactly planar.
        val ringHeight = poleHeight * (1.0 - cos(PI / 5)) / (1.0 + cos(PI / 5))
        val ring = (0..9).map { i ->
            DiceVector(cos(i * PI / 5), sin(i * PI / 5), if (i % 2 == 0) ringHeight else -ringHeight)
        }
        val faces = buildList {
            for (i in 0..4) {
                val start = i * 2
                add((i + 1) to intArrayOf(10, start, (start + 1) % 10, (start + 2) % 10))
                val opposite = (start + 5) % 10
                add((10 - i) to intArrayOf(11, opposite, (opposite + 1) % 10, (opposite + 2) % 10))
            }
        }
        DiceSolid(ring + listOf(DiceVector(0.0, 0.0, poleHeight), DiceVector(0.0, 0.0, -poleHeight)), faces)
    }

    fun forType(type: DiceType): DiceSolid = when (type) {
        DiceType.D6 -> cube
        DiceType.D8 -> octahedron
        DiceType.D10 -> trapezohedron
    }
}
