package com.dejitarunoseireinoapuri.saikorodojo.feature.rules.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RulesStringsTest {
    @Test
    fun `rules are present in all locales`() {
        val localeFiles = listOf(
            "app/src/main/res/values/strings.xml",
            "app/src/main/res/values-es/strings.xml",
            "app/src/main/res/values-ca/strings.xml"
        ).map(::resolveProjectPath)

        localeFiles.forEach { file ->
            val content = file.readText()
            assertTrue(content.contains("name=\"rules_main_game_title\""))
            assertTrue(content.contains("name=\"rules_cards_title\""))
            assertTrue(content.contains("name=\"rules_card_adjust_title\""))
            assertTrue(content.contains("name=\"rules_minigame_blackjack_title\""))
        }
    }

    @Test
    fun `rules describe main game before minigames and avoid ad mentions`() {
        val content = resolveProjectPath("app/src/main/res/values/strings.xml").readText()

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
    fun `rules text does not use bullet prefixes`() {
        val localeFiles = listOf(
            "app/src/main/res/values/strings.xml",
            "app/src/main/res/values-es/strings.xml",
            "app/src/main/res/values-ca/strings.xml"
        ).map(::resolveProjectPath)

        localeFiles.forEach { file ->
            val content = file.readText()
            val rulesSection = content.substring(
                startIndex = content.indexOf("name=\"rules_main_game_title\""),
                endIndex = content.indexOf("</resources>")
            )
            assertFalse(rulesSection.contains("•"))
            assertFalse(rulesSection.contains("\n- "))
        }
    }

    @Test
    fun `rules wording updates are applied in all locales`() {
        val defaultContent = resolveProjectPath("app/src/main/res/values/strings.xml").readText()
        val esContent = resolveProjectPath("app/src/main/res/values-es/strings.xml").readText()
        val caContent = resolveProjectPath("app/src/main/res/values-ca/strings.xml").readText()

        assertTrue(defaultContent.contains("6-, 8-, and 10-sided dice"))
        assertTrue(esContent.contains("dados de 6, 8 y 10 caras"))
        assertTrue(caContent.contains("daus de 6, 8 i 10 cares"))

        assertFalse(defaultContent.contains("Global inventory"))
        assertFalse(esContent.contains("Inventario global"))

        assertTrue(defaultContent.contains("Each use consumes that card."))
        assertTrue(esContent.contains("Cada uso consume esa carta."))
        assertTrue(caContent.contains("Cada ús consumeix aquesta carta."))

        assertTrue(defaultContent.contains("name=\"rules_card_adjust_title\""))
        assertTrue(esContent.contains("name=\"rules_card_adjust_title\""))
        assertTrue(caContent.contains("name=\"rules_card_adjust_title\""))
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
