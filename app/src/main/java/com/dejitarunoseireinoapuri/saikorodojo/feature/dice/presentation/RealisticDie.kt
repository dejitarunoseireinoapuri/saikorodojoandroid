package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.Turquoise

@Composable
internal fun RealisticDie(
    value: Int,
    type: DiceType,
    size: Dp,
    modifier: Modifier = Modifier,
    motion: State<Float>? = null,
    index: Int = 0,
    travelX: Dp = 0.dp,
    travelY: Dp = 0.dp,
    selected: Boolean = false,
    highlightResult: Boolean = true,
    numberColor: Color? = null,
    numberModifier: Modifier = Modifier
) {
    val description = stringResource(R.string.cd_dice_face, value)
    val result = rememberUpdatedState(value)
    val palette = dicePalette(type)
    val light = palette.light
    val dark = palette.dark
    val ink = numberColor ?: palette.ink
    val renderer = remember(type, index, light, dark, ink) {
        DiceSolidRenderer(DiceSolids.forType(type), index, light.toArgb(), dark.toArgb(), ink.toArgb())
    }
    Box(
        modifier = modifier.size(size).then(
            if (selected) Modifier.border(2.dp, Turquoise, RoundedCornerShape(12.dp)) else Modifier
        ).graphicsLayer {
            val travel = diceTravelFraction(motion?.value ?: 1f)
            translationX = travelX.toPx() * travel
            translationY = travelY.toPx() * travel
        },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer {
            val height = diceBounceHeight(motion?.value ?: 1f)
            alpha = 0.28f - height * 0.8f
            scaleX = 1f + height
            scaleY = 1f + height
        }.drawWithCache {
            val shadow = Brush.radialGradient(
                listOf(Color.Black, Color.Transparent),
                center = Offset(this.size.width * 0.5f, this.size.height * 0.77f),
                radius = (this.size.width * 0.42f).coerceAtLeast(0.1f)
            )
            onDrawBehind {
                drawOval(shadow, Offset(this.size.width * 0.12f, this.size.height * 0.58f),
                    Size(this.size.width * 0.76f, this.size.height * 0.33f))
            }
        })
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                val height = diceBounceHeight(motion?.value ?: 1f)
                translationY = -height * this.size.height
                scaleX = 1f - height * 0.20f
                scaleY = scaleX
            }.drawWithCache {
                onDrawBehind {
                    renderer.draw(drawContext.canvas.nativeCanvas, this.size.width, this.size.height,
                        motion?.value ?: 1f, result.value, highlightResult)
                }
            }.semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            // The accessible result has no overlay: all visible text is attached to faces.
            Box(numberModifier.size(size * 0.3f).semantics {
                text = AnnotatedString(value.toString())
            })
        }
    }
}
