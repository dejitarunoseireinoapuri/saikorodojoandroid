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
    fun `eight sided drawables keep rounded triangle geometry across states`() {
        val roundedTrianglePath = "M512,151.2C530,151.2 546,161 555,177L857,780C866,798 859,820 842,828L182,828C165,820 158,798 167,780L469,177C478,161 494,151.2 512,151.2Z"
        val drawablePaths = listOf(
            "app/src/main/res/drawable/eigth_sides.xml",
            "app/src/main/res/drawable/eigth_sides_selected.xml",
            "app/src/main/res/drawable/eigth_sides_contrast.xml",
            "app/src/main/res/drawable/eigth_sides_set_value.xml"
        ).map { resolveProjectPath(it) }

        drawablePaths.forEach { file ->
            val contents = file.readText()
            assertTrue(contents.contains(roundedTrianglePath))
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
