package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val actionLabelRes: Int = R.string.apply,
    val count: Int = 1
)

internal val DefaultCardSize = DpSize(width = 200.dp, height = 280.dp)

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
    iconAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    val shape = RoundedCornerShape(8.dp)
    val bottomPadding = if (showActionButton) 40.dp else 12.dp
    val countLayout = resolveCountLayout(
        showTitle = showTitle,
        showDescription = showDescription,
        showActionButton = showActionButton
    )
    Box(
        modifier = modifier
            .size(cardSize)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(width = 2.dp, color = Color.Black, shape = shape)
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .align(iconAlignment)
                )
            }
            if (showDescription) {
                Text(
                    text = stringResource(card.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        if (showActionButton) {
            OutlinedButton(
                onClick = onApplyClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
            ) {
                Text(
                    text = stringResource(card.actionLabelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "x",
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "x",
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
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
            id = CardId.RETRY,
            titleRes = R.string.card_retry_title,
            descriptionRes = R.string.card_retry_description,
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
