package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

data class DiceRollTurnPlan(
    val steps: Int,
    val selectedIndices: List<Int>,
    val selectedDiceTypes: List<DiceType>
)

class RunDiceRollTurnUseCase {
    fun prepare(
        durationMs: Long,
        tickMs: Long,
        diceTypes: List<DiceType>,
        selectedIndices: List<Int>? = null
    ): DiceRollTurnPlan {
        val steps = (durationMs / tickMs).coerceAtLeast(1L).toInt()
        val indices = selectedIndices ?: diceTypes.indices.toList()
        val selectedDiceTypes = indices.mapNotNull { diceTypes.getOrNull(it) }
        return DiceRollTurnPlan(
            steps = steps,
            selectedIndices = indices,
            selectedDiceTypes = selectedDiceTypes
        )
    }

    fun applyValues(
        currentValues: List<Int>,
        selectedIndices: List<Int>,
        rolledValues: List<Int>
    ): List<Int> {
        val updatedValues = currentValues.toMutableList()
        selectedIndices.forEachIndexed { listIndex, dieIndex ->
            if (dieIndex in updatedValues.indices) {
                updatedValues[dieIndex] = rolledValues.getOrNull(listIndex) ?: updatedValues[dieIndex]
            }
        }
        return updatedValues
    }
}
