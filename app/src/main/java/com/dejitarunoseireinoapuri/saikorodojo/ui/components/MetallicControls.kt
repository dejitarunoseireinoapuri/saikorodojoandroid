package com.dejitarunoseireinoapuri.saikorodojo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

private val DefaultMetallicShape = RoundedCornerShape(16.dp)

private fun Modifier.metallicFinish(
    shape: Shape,
    color: Color,
    enabled: Boolean,
    pressProgress: State<Float>
): Modifier = graphicsLayer {
    this.shape = shape
    clip = false
    translationY = if (enabled) 3.dp.toPx() * pressProgress.value else 0f
    shadowElevation = if (enabled) (7f - 5f * pressProgress.value).dp.toPx() else 0f
}.drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val depth = if (enabled) 3.dp.toPx() else 0f
    val baseColor = lerp(color, Color.Black, 0.30f)
    val face = Brush.verticalGradient(
        0f to lerp(color, Color.White, if (enabled) 0.23f else 0.04f),
        0.42f to color,
        1f to lerp(color, Color.Black, if (enabled) 0.12f else 0.02f),
        endY = size.height
    )

    onDrawWithContent {
        // Keep the base stationary while the face sinks into it on press.
        translate(top = depth * (1f - pressProgress.value)) {
            drawOutline(outline, color = baseColor)
        }
        drawOutline(outline, brush = face)
        drawContent()
    }
}

@Composable
fun MetallicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultMetallicShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress = animateFloatAsState(
        targetValue = if (enabled && isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 110),
        label = "buttonDepth"
    )
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    Button(
        onClick = onClick,
        modifier = modifier.metallicFinish(shape, containerColor, enabled, pressProgress),
        enabled = enabled,
        shape = shape,
        colors = colors.copy(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        elevation = null,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun MetallicTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    MetallicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors.copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        ),
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun MetallicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}
