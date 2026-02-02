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
        )

        drawablePaths.forEach { path ->
            val contents = File(path).readText()
            assertTrue(contents.contains(expectedColor))
        }
    }
}
