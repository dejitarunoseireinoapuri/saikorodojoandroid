package com.dejitarunoseireinoapuri.saikorodojo.feature.settings.presentation

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SettingsStringsTest {
    @Test
    fun `settings manage ads button is translated in spanish and catalan`() {
        val spanish = resolveProjectPath("app/src/main/res/values-es/strings.xml").readText()
        val catalan = resolveProjectPath("app/src/main/res/values-ca/strings.xml").readText()

        assertTrue(spanish.contains("<string name=\"settings_manage_ads_button\">Gestionar opciones de anuncios</string>"))
        assertTrue(catalan.contains("<string name=\"settings_manage_ads_button\">Gestiona les opcions d\\'anuncis</string>"))
    }

    @Test
    fun `settings title and back are translated in spanish and catalan`() {
        val spanish = resolveProjectPath("app/src/main/res/values-es/strings.xml").readText()
        val catalan = resolveProjectPath("app/src/main/res/values-ca/strings.xml").readText()

        assertTrue(spanish.contains("<string name=\"settings_screen_title\">Ajustes</string>"))
        assertTrue(spanish.contains("<string name=\"settings_screen_back\">Atrás</string>"))

        assertTrue(catalan.contains("<string name=\"settings_screen_title\">Ajustos</string>"))
        assertTrue(catalan.contains("<string name=\"settings_screen_back\">Enrere</string>"))
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
