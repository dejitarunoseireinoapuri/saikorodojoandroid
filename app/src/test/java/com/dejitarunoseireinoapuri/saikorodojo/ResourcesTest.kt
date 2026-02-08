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
    fun `move sound effect resource is available`() {
        assertNotEquals(0, R.raw.move)
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
