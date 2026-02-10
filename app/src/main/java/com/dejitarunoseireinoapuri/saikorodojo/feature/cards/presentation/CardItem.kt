package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme

data class CardUiModel(
    val id: CardId,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val actionLabelRes: Int = R.string.apply,
    val count: Int = 1
)

internal val DefaultCardSize = DpSize(width = 208.dp, height = 278.dp)

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    card: CardUiModel,
    onApplyClick: () -> Unit,
    cardSize: DpSize = DefaultCardSize,
    showDescription: Boolean = true,
    showActionButton: Boolean = true,
    showTitle: Boolean = true,
    showCount: Boolean = false,
    iconAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    isEnabled: Boolean = true,
    description: AnnotatedString? = null,
    descriptionTextAlign: TextAlign = TextAlign.Start
) {
    val shape = RoundedCornerShape(20.dp)
    val outerBorderWidth = 1.dp
    val innerBorderWidth = 4.dp
    val bottomPadding = if (showActionButton) 40.dp else 12.dp
    val countLayout = resolveCountLayout(
        showTitle = showTitle,
        showDescription = showDescription,
        showActionButton = showActionButton
    )
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiary,
            Color.Black
        )
    )
    Box(
        modifier = modifier
            .size(cardSize)
            .background(Color.Black, shape)
            .padding(outerBorderWidth)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, shape)
                .padding(innerBorderWidth)
                .clip(shape)
                .background(cardBrush)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (showTitle) {
                    Text(
                        text = stringResource(card.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = rememberFittingTitleSize(
                                text = stringResource(card.titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                maxWidthDp = cardSize.width - 24.dp
                            )
                        ),
                        color = Color.White,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showCount) {
                    CountWithIcon(
                        count = card.count,
                        iconRes = card.iconRes,
                        iconAlignment = iconAlignment,
                        layout = countLayout
                    )
                } else {
                    Icon(
                        painter = painterResource(card.iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .align(iconAlignment)
                    )
                }
                if (showDescription) {
                    Text(
                        text = description ?: AnnotatedString(stringResource(card.descriptionRes)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = descriptionTextAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (showActionButton) {
                Button(
                    onClick = onApplyClick,
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp, end = 4.dp)
                        .defaultMinSize(minWidth = 120.dp, minHeight = 52.dp)
                ) {
                    Text(
                        text = stringResource(card.actionLabelRes),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CountWithIcon(
    count: Int,
    @DrawableRes iconRes: Int,
    iconAlignment: Alignment.Horizontal,
    layout: CountLayout
) {
    val anchorAlignment = if (iconAlignment == Alignment.Start) {
        Alignment.Start
    } else {
        Alignment.CenterHorizontally
    }
    val textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    if (layout == CountLayout.Horizontal) {
        Row(
            modifier = Modifier.align(anchorAlignment),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = textStyle,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "x",
                style = textStyle,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .align(anchorAlignment)
                .padding(top = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = textStyle,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "x",
                style = textStyle,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

internal enum class CountLayout {
    Horizontal,
    Vertical
}

internal fun resolveCountLayout(
    showTitle: Boolean,
    showDescription: Boolean,
    showActionButton: Boolean
): CountLayout {
    return if (showTitle || showDescription || showActionButton) {
        CountLayout.Horizontal
    } else {
        CountLayout.Vertical
    }
}

@Composable
private fun rememberFittingTitleSize(
    text: String,
    style: TextStyle,
    maxWidthDp: androidx.compose.ui.unit.Dp
): TextUnit {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidthDp.roundToPx() }
    val candidateSizes = titleFontSizes()
    return candidateSizes.firstOrNull { fontSize ->
        val measured = textMeasurer.measure(
            text = text,
            style = style.copy(fontSize = fontSize),
            maxLines = 1
        )
        measured.size.width <= maxWidthPx
    } ?: candidateSizes.last()
}

internal fun titleFontSizes(): List<TextUnit> {
    return listOf(18.sp, 17.sp, 16.sp, 15.sp, 14.sp, 13.sp, 12.sp)
}

internal fun defaultCardUiModels(): List<CardUiModel> {
    return listOf(
        CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust
        ),
        CardUiModel(
            id = CardId.FLIP_FACE,
            titleRes = R.string.card_flip_face_title,
            descriptionRes = R.string.card_flip_face_description,
            iconRes = R.drawable.ic_card_flip
        ),
        CardUiModel(
            id = CardId.REROLL_SINGLE,
            titleRes = R.string.card_reroll_single_title,
            descriptionRes = R.string.card_reroll_single_description,
            iconRes = R.drawable.ic_card_reroll_single
        ),
        CardUiModel(
            id = CardId.REROLL_ALL,
            titleRes = R.string.card_reroll_all_title,
            descriptionRes = R.string.card_reroll_all_description,
            iconRes = R.drawable.ic_card_reroll_all
        ),
        CardUiModel(
            id = CardId.SET_VALUE,
            titleRes = R.string.card_set_value_title,
            descriptionRes = R.string.card_set_value_description,
            iconRes = R.drawable.ic_card_set_value
        ),
        CardUiModel(
            id = CardId.REPEAT_LAST,
            titleRes = R.string.card_repeat_last_title,
            descriptionRes = R.string.card_repeat_last_description,
            iconRes = R.drawable.ic_card_repeat_last
        ),
        CardUiModel(
            id = CardId.MINIGAMES,
            titleRes = R.string.card_minigames_title,
            descriptionRes = R.string.card_minigames_description,
            iconRes = R.drawable.ic_card_retry
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1800)
@Composable
private fun CardItemPreview() {
    SaikoroDojoTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            defaultCardUiModels().forEach { card ->
                CardItem(card = card, onApplyClick = {})
            }
        }
    }
}
