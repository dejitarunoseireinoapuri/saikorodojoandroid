package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain

data class HigherLowerRoll(
    val values: List<Int>
) {
    val sum: Int = values.sum()
}
