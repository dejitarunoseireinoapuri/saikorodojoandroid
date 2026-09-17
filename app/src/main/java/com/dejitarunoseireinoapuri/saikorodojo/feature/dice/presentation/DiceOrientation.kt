package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Mutable scratch rotation: the renderer reuses it and its matrix on every frame. */
internal class DiceQuaternion(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var w: Double = 1.0
) {
    fun setEuler(rx: Double, ry: Double, rz: Double): DiceQuaternion {
        val cx = cos(rx / 2)
        val sx = sin(rx / 2)
        val cy = cos(ry / 2)
        val sy = sin(ry / 2)
        val cz = cos(rz / 2)
        val sz = sin(rz / 2)
        x = sx * cy * cz - cx * sy * sz
        y = cx * sy * cz + sx * cy * sz
        z = cx * cy * sz - sx * sy * cz
        w = cx * cy * cz + sx * sy * sz
        return this
    }

    fun setBasis(u: DiceVector, v: DiceVector, n: DiceVector): DiceQuaternion {
        val trace = u.x + v.y + n.z
        when {
            trace > 0 -> {
                val s = sqrt(trace + 1) * 2
                w = s / 4
                x = (n.y - v.z) / s
                y = (u.z - n.x) / s
                z = (v.x - u.y) / s
            }
            u.x > v.y && u.x > n.z -> {
                val s = sqrt(1 + u.x - v.y - n.z) * 2
                w = (n.y - v.z) / s
                x = s / 4
                y = (u.y + v.x) / s
                z = (u.z + n.x) / s
            }
            v.y > n.z -> {
                val s = sqrt(1 + v.y - u.x - n.z) * 2
                w = (u.z - n.x) / s
                x = (u.y + v.x) / s
                y = s / 4
                z = (v.z + n.y) / s
            }
            else -> {
                val s = sqrt(1 + n.z - u.x - v.y) * 2
                w = (v.x - u.y) / s
                x = (u.z + n.x) / s
                y = (v.z + n.y) / s
                z = s / 4
            }
        }
        return this
    }

    fun setProduct(a: DiceQuaternion, b: DiceQuaternion): DiceQuaternion {
        val nx = a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y
        val ny = a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x
        val nz = a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w
        val nw = a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z
        x = nx
        y = ny
        z = nz
        w = nw
        return this
    }

    fun setRollAngle(angle: Double, index: Int): DiceQuaternion {
        val direction = if (index % 2 == 0) 1.0 else -1.0
        val axisX = 0.65
        val axisY = 0.72 * direction
        val axisZ = 0.24 + (index % 3) * 0.08
        val factor = sin(angle / 2) / sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
        x = axisX * factor
        y = axisY * factor
        z = axisZ * factor
        w = cos(angle / 2)
        return this
    }

    fun writeMatrix(out: DoubleArray) {
        out[0] = 1 - 2 * (y * y + z * z)
        out[1] = 2 * (x * y - z * w)
        out[2] = 2 * (x * z + y * w)
        out[3] = 2 * (x * y + z * w)
        out[4] = 1 - 2 * (x * x + z * z)
        out[5] = 2 * (y * z - x * w)
        out[6] = 2 * (x * z - y * w)
        out[7] = 2 * (y * z + x * w)
        out[8] = 1 - 2 * (x * x + y * y)
    }
}

internal fun diceRemainingAngle(progress: Float, index: Int): Double {
    val remaining = 1.0 - progress.coerceIn(0f, 1f)
    return (9.5 + (index % 3) * 1.2) * remaining * remaining
}

/** One decelerating rotation is planned around the locked result, with no landing correction. */
internal fun setDiceOrientation(
    out: DiceQuaternion, scratch: DiceQuaternion, solid: DiceSolid,
    progress: Float, result: Int, index: Int
) {
    scratch.setRollAngle(diceRemainingAngle(progress, index), index)
    out.setProduct(scratch, solid.restingPoses[result.coerceIn(1, solid.faces.size) - 1])
}
