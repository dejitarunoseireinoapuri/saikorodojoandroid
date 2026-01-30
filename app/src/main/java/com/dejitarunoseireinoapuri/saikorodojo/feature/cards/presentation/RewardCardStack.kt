package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun RewardCardStack(
    cards: List<CardUiModel>,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) return
    var expandedIndex by remember(cards) { mutableStateOf<Int?>(null) }
    val cardSize = DpSize(width = 208.dp, height = 278.dp)
    val stackSpacing = 120.dp
    val stackRise = 12.dp
    BoxWithConstraints(modifier = modifier) {
        val totalWidth = cardSize.width + stackSpacing * (cards.size - 1).coerceAtLeast(0)
        val startX = (maxWidth - totalWidth) / 2f
        val centerY = (maxHeight - cardSize.height) / 2f
        cards.forEachIndexed { index, card ->
            val isExpanded = expandedIndex == index
            val positionX = startX + stackSpacing * index.toFloat()
            val positionY = centerY - stackRise * index.toFloat()
            Box(
                modifier = Modifier
                    .offset(x = positionX, y = positionY)
                    .zIndex(index.toFloat())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedIndex = if (isExpanded) null else index
                    }
            ) {
                CardItem(
                    card = card,
                    cardSize = cardSize,
                    showDescription = isExpanded,
                    showActionButton = false,
                    showTitle = isExpanded,
                    showCount = false,
                    onApplyClick = {}
                )
            }
        }
    }
}
