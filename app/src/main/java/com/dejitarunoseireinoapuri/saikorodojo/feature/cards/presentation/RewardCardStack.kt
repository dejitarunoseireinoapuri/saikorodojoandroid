package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

internal const val REWARD_CARD_STACK_TAG = "reward_card_stack"
internal const val REWARD_CARD_TAG_PREFIX = "reward_card"
internal val RewardCardExpandedKey = SemanticsPropertyKey<Boolean>("RewardCardExpanded")
internal var SemanticsPropertyReceiver.rewardCardExpanded by RewardCardExpandedKey

@Composable
fun RewardCardStack(
    cards: List<CardUiModel>,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) return
    var expandedIndex by remember(cards) { mutableIntStateOf(cards.lastIndex) }
    val cardSize = DpSize(width = 208.dp, height = 278.dp)
    val stackSpacing = 40.dp
    val stackRise = 32.dp
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag(REWARD_CARD_STACK_TAG)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (expandedIndex != cards.lastIndex) {
                        expandedIndex = cards.lastIndex
                    }
                }
        )
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
                    .zIndex(if (isExpanded) 2f else 1f + index * 0.01f)
                    .testTag("${REWARD_CARD_TAG_PREFIX}_$index")
                    .semantics { rewardCardExpanded = isExpanded }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedIndex = if (isExpanded) cards.lastIndex else index
                    }
            ) {
                CardItem(
                    card = card,
                    cardSize = cardSize,
                    showDescription = isExpanded,
                    showActionButton = false,
                    showTitle = isExpanded,
                    showCount = false,
                    iconAlignment = if (isExpanded) {
                        Alignment.CenterHorizontally
                    } else {
                        Alignment.Start
                    },
                    onApplyClick = {}
                )
            }
        }
    }
}
