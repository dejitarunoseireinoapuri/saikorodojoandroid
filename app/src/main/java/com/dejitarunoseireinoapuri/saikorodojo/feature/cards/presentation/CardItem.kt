package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ExposurePlus1
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme

data class CardUiModel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    @StringRes val actionLabelRes: Int = R.string.apply
)

internal val DefaultCardSize = DpSize(width = 180.dp, height = 260.dp)

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    card: CardUiModel,
    onApplyClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(DefaultCardSize)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(width = 2.dp, color = Color.Black, shape = shape)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(card.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(card.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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

internal fun defaultCardUiModels(): List<CardUiModel> {
    return listOf(
        CardUiModel(
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            icon = Icons.Default.ExposurePlus1
        ),
        CardUiModel(
            titleRes = R.string.card_flip_face_title,
            descriptionRes = R.string.card_flip_face_description,
            icon = Icons.Default.Flip
        ),
        CardUiModel(
            titleRes = R.string.card_reroll_single_title,
            descriptionRes = R.string.card_reroll_single_description,
            icon = Icons.Default.Casino
        ),
        CardUiModel(
            titleRes = R.string.card_reroll_all_except_one_title,
            descriptionRes = R.string.card_reroll_all_except_one_description,
            icon = Icons.Default.RestartAlt
        ),
        CardUiModel(
            titleRes = R.string.card_set_value_title,
            descriptionRes = R.string.card_set_value_description,
            icon = Icons.Default.PushPin
        ),
        CardUiModel(
            titleRes = R.string.card_repeat_last_title,
            descriptionRes = R.string.card_repeat_last_description,
            icon = Icons.Default.Replay
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
