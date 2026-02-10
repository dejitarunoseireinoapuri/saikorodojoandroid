package com.dejitarunoseireinoapuri.saikorodojo.feature.rules.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RulesStringsTest {
    @Test
    fun `rules describe main game before minigames and avoid ads`() {
        val stringsFile = resolveProjectPath("app/src/main/res/values/strings.xml")
        val content = stringsFile.readText()

        val mainGameIndex = content.indexOf("name=\"rules_main_game_title\"")
        val minigamesIndex = content.indexOf("name=\"rules_minigames_title\"")

        assertTrue(mainGameIndex in 0 until minigamesIndex)

        val minigamesBodyStart = content.indexOf("name=\"rules_minigames_body\"")
        val minigamesBodyEnd = content.indexOf("</string>", minigamesBodyStart)
        val minigamesBody = content.substring(minigamesBodyStart, minigamesBodyEnd)

        assertFalse(minigamesBody.contains("anuncio", ignoreCase = true))
        assertFalse(minigamesBody.contains("ad", ignoreCase = true))
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
