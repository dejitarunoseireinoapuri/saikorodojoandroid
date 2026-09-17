package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SoftGold
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.NightInk

/** Orthographic projection keeps polygons and printed numbers on the same face plane. */
internal class DiceSolidRenderer(
    private val solid: DiceSolid,
    private val index: Int,
    private val lightColor: Int,
    private val darkColor: Int,
    inkColor: Int
) {
    private val rotation = DiceQuaternion()
    private val scratch = DiceQuaternion()
    private val matrix = DoubleArray(9)
    private val projected = FloatArray(solid.vertices.size * 2)
    private val path = Path()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        color = 0x55FFFFFF
    }
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inkColor
        textSize = 100f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val resultInk = Paint(ink).apply { color = SoftGold.toArgb() }
    private val resultInkOutline = Paint(ink).apply {
        color = NightInk.toArgb()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 4f
    }
    private val baseline = -(ink.fontMetrics.ascent + ink.fontMetrics.descent) / 2f
    private val labels = solid.faces.map { it.value.toString() }
    private val textMatrix = Matrix()
    private val textTransform = FloatArray(9).apply { this[8] = 1f }

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        progress: Float,
        result: Int,
        highlightResult: Boolean
    ) {
        if (width <= 0 || height <= 0) return
        setDiceOrientation(rotation, scratch, solid, progress, result, index)
        rotation.writeMatrix(matrix)
        val scale = (minOf(width, height) * 0.43 / solid.radius).toFloat()
        val cx = width * 0.5f
        val cy = height * 0.51f
        val resultGoldAlpha = diceResultGoldAlpha(progress, highlightResult)
        for (i in solid.vertices.indices) {
            val vertex = solid.vertices[i]
            projected[i * 2] = cx + component(0, vertex) * scale
            projected[i * 2 + 1] = cy - component(3, vertex) * scale
        }
        edge.strokeWidth = (width * 0.008f).coerceAtLeast(0.6f)
        // Convex solids need no depth sorting after back-facing polygons are culled.
        for (i in solid.faces.indices) {
            val face = solid.faces[i]
            val nz = component(6, face.normal)
            if (nz <= 0.001f) continue
            val nx = component(0, face.normal)
            val ny = component(3, face.normal)
            val illumination = (-0.35f * nx + 0.45f * ny + 0.82f * nz).coerceIn(0f, 1f)
            fill.color = shade(0.18f + 0.78f * illumination)
            path.rewind()
            for (j in face.vertices.indices) {
                val vertexIndex = face.vertices[j] * 2
                if (j == 0) path.moveTo(projected[vertexIndex], projected[vertexIndex + 1])
                else path.lineTo(projected[vertexIndex], projected[vertexIndex + 1])
            }
            path.close()
            canvas.drawPath(path, fill)
            canvas.drawPath(path, edge)
            val isResult = face.value == result
            if (nz <= 0.07f) continue
            val letteringScale = (face.labelRadius * diceNumberScale() * scale / 100).toFloat()
            ink.alpha = 255
            textTransform[0] = component(0, face.right) * letteringScale
            textTransform[1] = -component(0, face.up) * letteringScale
            textTransform[2] = cx + component(0, face.center) * scale
            textTransform[3] = -component(3, face.right) * letteringScale
            textTransform[4] = component(3, face.up) * letteringScale
            textTransform[5] = cy - component(3, face.center) * scale
            textMatrix.setValues(textTransform)
            val save = canvas.save()
            canvas.clipPath(path)
            canvas.concat(textMatrix)
            canvas.drawText(labels[i], 0f, baseline, ink)
            if (isResult && resultGoldAlpha > 0f) {
                // A thin dark text outline keeps gold legible on the ivory D6.
                resultInkOutline.alpha = (255 * resultGoldAlpha).toInt()
                resultInk.alpha = resultInkOutline.alpha
                canvas.drawText(labels[i], 0f, baseline, resultInkOutline)
                canvas.drawText(labels[i], 0f, baseline, resultInk)
            }
            canvas.restoreToCount(save)
        }
    }

    private fun component(row: Int, vector: DiceVector): Float =
        (matrix[row] * vector.x + matrix[row + 1] * vector.y + matrix[row + 2] * vector.z).toFloat()

    private fun shade(amount: Float): Int = (0xFF shl 24) or
        (channel(16, amount) shl 16) or (channel(8, amount) shl 8) or channel(0, amount)

    private fun channel(shift: Int, amount: Float): Int {
        val low = (darkColor ushr shift) and 255
        val high = (lightColor ushr shift) and 255
        return (low + (high - low) * amount).toInt().coerceIn(0, 255)
    }
}
