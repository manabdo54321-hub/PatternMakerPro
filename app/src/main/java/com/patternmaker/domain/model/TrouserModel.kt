package com.patternmaker.domain.model

data class TrouserModel(
    val id: String,
    val nameAr: String,
    val frontCrotchRatio: Float,
    val backCrotchRatio: Float,
    val ease: Float,
    val legTaper: Float,
    val hasWaistBand: Boolean = true,
    val hasLegBand: Boolean = true
) {
    companion object {
        val SWEATPANTS = TrouserModel(
            id = "sweatpants",
            nameAr = "رياضي",
            frontCrotchRatio = 0.0625f,
            backCrotchRatio = 0.125f,
            ease = 6f,
            legTaper = 0.2f
        )
        val ARABIC = TrouserModel(
            id = "arabic",
            nameAr = "عربي واسع",
            frontCrotchRatio = 0.05f,
            backCrotchRatio = 0.10f,
            ease = 10f,
            legTaper = 0f,
            hasLegBand = false
        )
        val AFGHAN = TrouserModel(
            id = "afghan",
            nameAr = "أفغاني",
            frontCrotchRatio = 0.055f,
            backCrotchRatio = 0.11f,
            ease = 8f,
            legTaper = 0f,
            hasLegBand = false
        )
        val ALL = listOf(SWEATPANTS, ARABIC, AFGHAN)
    }
}
