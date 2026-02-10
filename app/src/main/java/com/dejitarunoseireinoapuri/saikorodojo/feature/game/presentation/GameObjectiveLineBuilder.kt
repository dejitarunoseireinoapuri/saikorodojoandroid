package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective

internal fun buildObjectiveLines(
    objective: LevelObjective,
    diceValues: List<Int>,
    diceSides: List<Int>
): List<ObjectiveLineUiState> {
    val selectedCount = diceValues.size
    return objective.conditions.map { condition ->
        val text = objectiveLineText(condition)
        val explainText = objectiveLineExplainText(condition, selectedCount)
        ObjectiveLineUiState(
            text = text,
            explainText = explainText,
            isMet = condition.isMet(diceValues, diceSides)
        )
    }
}
