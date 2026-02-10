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
    fun `selected dice drawables reference selected color resources`() {
        val drawablePaths = listOf(
            "app/src/main/res/drawable/six_sides_selected.xml",
            "app/src/main/res/drawable/eigth_sides_selected.xml",
            "app/src/main/res/drawable/ten_sides_selected.xml"
        ).map { resolveProjectPath(it) }

        drawablePaths.forEach { file ->
            val contents = file.readText()
            assertTrue(contents.contains("@color/dice_selected_outer"))
            assertTrue(contents.contains("@color/dice_selected_inner"))
        }
    }



    @Test
    fun `eight sided drawables use the octagon geometry across states`() {
        val octagonPath = "M350,120L674,120L904,350L904,674L674,904L350,904L120,674L120,350Z"
        val drawablePaths = listOf(
            "app/src/main/res/drawable/eigth_sides.xml",
            "app/src/main/res/drawable/eigth_sides_selected.xml",
            "app/src/main/res/drawable/eigth_sides_contrast.xml",
            "app/src/main/res/drawable/eigth_sides_set_value.xml"
        ).map { resolveProjectPath(it) }

        drawablePaths.forEach { file ->
            val contents = file.readText()
            assertTrue(contents.contains(octagonPath))
        }
    }

    @Test
    fun `ten sided set value drawable keeps ten sided geometry`() {
        val file = resolveProjectPath("app/src/main/res/drawable/ten_sides_set_value.xml")
        val contents = file.readText()

        assertTrue(contents.contains("M550.8,914.1L910.5,588.3"))
        assertTrue(contents.contains("@color/dice_set_value_outer"))
        assertTrue(contents.contains("@color/dice_set_value_inner"))
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
