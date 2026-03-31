package com.patternmaker.domain.model

data class PlacedPiece(
    val piece: PatternPiece,
    val x: Float,
    val y: Float,
    val rotation: Float = 0f
)

data class FabricSettings(
    val width: Float = 150f,
    val length: Float? = null,      // null = أوتوماتيك حسب القطع
    val quantity: Int = 1,          // عدد الأطقم
    val gap: Float = 1.5f,          // مسافة بين القطع
    val allowRotation: Boolean = true,
    val respectGrainLine: Boolean = true  // احترام اتجاه الخامة
)

data class NestingLayout(
    val fabricWidth: Float,
    val fabricLength: Float,
    val placedPieces: List<PlacedPiece> = emptyList(),
    val settings: FabricSettings = FabricSettings()
) {
    // مساحة القطع الفعلية
    val usedArea: Float get() =
        placedPieces.sumOf { (it.piece.widthCm * it.piece.heightCm).toDouble() }.toFloat()

    // مساحة القماش الكلية
    val totalArea: Float get() = fabricWidth * fabricLength

    // نسبة الهدر
    val wastePercent: Float get() =
        if (totalArea > 0) ((totalArea - usedArea) / totalArea) * 100f else 0f

    // نسبة الاستخدام
    val efficiency: Float get() = 100f - wastePercent

    // عدد الأطقم
    val quantity: Int get() = settings.quantity
}
