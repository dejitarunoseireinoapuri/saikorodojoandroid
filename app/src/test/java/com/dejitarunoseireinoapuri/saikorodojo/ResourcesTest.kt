package com.dejitarunoseireinoapuri.saikorodojo

import org.junit.Assert.assertNotEquals
import org.junit.Test

class ResourcesTest {
    @Test
    fun `dice face drawables are available`() {
        assertNotEquals(0, R.drawable.six_sides)
        assertNotEquals(0, R.drawable.eigth_sides)
        assertNotEquals(0, R.drawable.ten_sides)
    }
}
