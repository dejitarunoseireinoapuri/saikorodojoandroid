package com.dejitarunoseireinoapuri.saikorodojo.feature.rules.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RulesStringsTest {
    @Test
    fun `rules describe main game before minigames and avoid ad mentions`() {
        val stringsFile = resolveProjectPath("app/src/main/res/values/strings.xml")
        val content = stringsFile.readText()

        val mainGameIndex = content.indexOf("name=\"rules_main_game_title\"")
        val minigamesIndex = content.indexOf("name=\"rules_minigames_title\"")

        assertTrue(mainGameIndex in 0 until minigamesIndex)

        val rulesSectionStart = content.indexOf("name=\"rules_minigames_body\"")
        val rulesSectionEnd = content.indexOf("name=\"rules_minigame_blackjack_body\"")
        val minigamesSection = content.substring(rulesSectionStart, rulesSectionEnd)

        assertFalse(minigamesSection.contains("anuncio", ignoreCase = true))
        assertFalse(minigamesSection.contains("watch an ad", ignoreCase = true))
    }

    @Test
    fun `rules include cards subsection and one subsection per minigame`() {
        val stringsFile = resolveProjectPath("app/src/main/res/values/strings.xml")
        val content = stringsFile.readText()

        assertTrue(content.contains("name=\"rules_cards_title\""))
        assertTrue(content.contains("name=\"rules_cards_body\""))
        assertTrue(content.contains("name=\"rules_minigame_odd_even_title\""))
        assertTrue(content.contains("name=\"rules_minigame_higher_lower_title\""))
        assertTrue(content.contains("name=\"rules_minigame_sequence_title\""))
        assertTrue(content.contains("name=\"rules_minigame_blackjack_title\""))
    }

    private fun resolveProjectPath(path: String): File {
        val projectRoot = requireNotNull(System.getProperty("user.dir")) {
            "user.dir is required to resolve project paths"
        }
        val projectDir = File(projectRoot)
        val direct = File(projectDir, path)
        if (direct.exists()) return direct
        val parent = projectDir.parentFile ?: projectDir
        return File(parent, path)
    }
}
