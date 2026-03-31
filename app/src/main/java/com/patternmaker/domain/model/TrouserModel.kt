package com.patternmaker.domain.model

data class TrouserModel(
    val id: String,
    val nameAr: String,
    val frontCrotchRatio: Float,
    val backCrotchRatio: Float,
    val ease: Float,
    val legTaper: Float,
    val hasWaistBand: Boolean = true,
    val hasLegBand: Boolean = true,
    val waistbandType: WaistbandType = WaistbandType.ELASTIC,
    val optionalPieces: List<OptionalPiece> = emptyList()
) {
    companion object {
        val SWEATPANTS = TrouserModel(
            id = "sweatpants",
            nameAr = "رياضي",
            frontCrotchRatio = 0.0625f,
            backCrotchRatio = 0.125f,
            ease = 6f,
            legTaper = 0.2f,
            waistbandType = WaistbandType.ELASTIC,
            optionalPieces = listOf(
                OptionalPiece.FRONT_POCKET,
                OptionalPiece.BACK_WELT_POCKET,
                OptionalPiece.BACK_YOKE,
                OptionalPiece.FLY,
                OptionalPiece.CARGO_POCKET
            )
        )
        val ARABIC = TrouserModel(
            id = "arabic",
            nameAr = "عربي واسع",
            frontCrotchRatio = 0.05f,
            backCrotchRatio = 0.10f,
            ease = 10f,
            legTaper = 0f,
            hasLegBand = false,
            waistbandType = WaistbandType.ELASTIC
        )
        val AFGHAN = TrouserModel(
            id = "afghan",
            nameAr = "أفغاني",
            frontCrotchRatio = 0.055f,
            backCrotchRatio = 0.11f,
            ease = 8f,
            legTaper = 0f,
            hasLegBand = false,
            waistbandType = WaistbandType.ELASTIC
        )
        val ALL = listOf(SWEATPANTS, ARABIC, AFGHAN)
    }
}

// ── نوع الكمر ────────────────────────────────────────────────
enum class WaistbandType(val nameAr: String, val widthCm: Float) {
    ELASTIC     ("مطاط كامل",        8f),   // مطاط على الكمر كله
    DRAWSTRING  ("حبل (ديكور)",      7f),   // حبل مع مطاط
    TUNNEL      ("نفق مطاط + حبل",   8f)    // نفق فيه مطاط وحبل مع بعض
}

// ── القطع الاختيارية ──────────────────────────────────────────
enum class OptionalPiece(
    val nameAr: String,
    val defaultEnabled: Boolean,
    val widthRatio: Float,   // نسبة من عرض الأمامية
    val heightCm: Float
) {
    FRONT_POCKET(
        nameAr         = "جيب أمامي",
        defaultEnabled = true,
        widthRatio     = 0.45f,
        heightCm       = 16f
    ),
    BACK_WELT_POCKET(
        nameAr         = "جيب خلفي (بطاقة)",
        defaultEnabled = true,
        widthRatio     = 0.35f,
        heightCm       = 2.5f   // عرض البطاقة فقط
    ),
    BACK_YOKE(
        nameAr         = "سفرة خلفية",
        defaultEnabled = false,
        widthRatio     = 1.0f,  // كامل عرض الخلفية
        heightCm       = 8f
    ),
    FLY(
        nameAr         = "بتلتة",
        defaultEnabled = false,
        widthRatio     = 0.15f,
        heightCm       = 18f
    ),
    CARGO_POCKET(
        nameAr         = "جيب كارجو",
        defaultEnabled = false,
        widthRatio     = 0.40f,
        heightCm       = 20f
    )
}
