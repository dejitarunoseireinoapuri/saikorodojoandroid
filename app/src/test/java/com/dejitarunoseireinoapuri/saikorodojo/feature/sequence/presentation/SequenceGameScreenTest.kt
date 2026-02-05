package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class SequenceGameScreenTest {

    @Test
    fun sequenceDecisionActionOrder_placesDiscardOnLeftAndSaveOnRight() {
        val actions = sequenceDecisionActionOrder()

        assertEquals(
            listOf(SequenceDecisionAction.Discard, SequenceDecisionAction.Save),
            actions
        )
    }
}
