package com.dejitarunoseireinoapuri.saikorodojo

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ResourcesTest {
    @Test
    fun `dice face drawables are available`() {
        assertNotEquals(0, R.drawable.six_sides)
        assertNotEquals(0, R.drawable.eigth_sides)
        assertNotEquals(0, R.drawable.ten_sides)
    }

    @Test
    fun `selected dice drawables use app primary color`() {
        val expectedColor = "#FF56BD88"
        val drawablePaths = listOf(
            "app/src/main/res/drawable/six_sides_selected.xml",
            "app/src/main/res/drawable/eigth_sides_selected.xml",
            "app/src/main/res/drawable/ten_sides_selected.xml"
        ).map { resolveProjectPath(it) }

        drawablePaths.forEach { file ->
            val contents = file.readText()
            assertTrue(contents.contains(expectedColor))
        }
    }

    private fun resolveProjectPath(path: String): File {
        val projectDir = File(System.getProperty("user.dir"))
        val direct = File(projectDir, path)
        if (direct.exists()) return direct
        val parent = projectDir.parentFile ?: projectDir
        return File(parent, path)
    }
}
