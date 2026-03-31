package com.patternmaker.domain.model

data class PlacedPiece(
    val piece: PatternPiece,
    val x: Float,
    val y: Float,
    val rotation: Float = 0f
)

data class NestingLayout(
    val fabricWidth: Float,
    val fabricLength: Float,
    val placedPieces: List<PlacedPiece> = emptyList()
) {
    val usedArea: Float get() =
        placedPieces.sumOf { (it.piece.widthCm * it.piece.heightCm).toDouble() }.toFloat()

    val totalArea: Float get() = fabricWidth * fabricLength

    val wastePercent: Float get() =
        if (totalArea > 0) ((totalArea - usedArea) / totalArea) * 100f else 0f
}
