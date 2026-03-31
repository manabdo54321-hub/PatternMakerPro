package com.patternmaker.domain.engine

import com.patternmaker.domain.model.NestingLayout
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.PlacedPiece

object NestingEngine {

    fun autoNest(
        pieces: List<PatternPiece>,
        fabricWidth: Float,
        allowRotation: Boolean = true
    ): NestingLayout {
        val placed = mutableListOf<PlacedPiece>()
        val gap = 1f

        // توسيع القطع حسب الكمية
        val expandedPieces = pieces.flatMap { piece ->
            List(piece.quantity) { piece }
        }.sortedByDescending { it.heightCm * it.widthCm }

        var currentX = gap
        var currentY = gap
        var rowHeight = 0f

        for (piece in expandedPieces) {
            val w = piece.widthCm
            val h = piece.heightCm

            // لو القطعة مش هتتناسب في العرض — جرب تدويرها
            val (finalW, finalH, rotation) = if (allowRotation && w > fabricWidth - gap * 2 && h <= fabricWidth - gap * 2) {
                Triple(h, w, 90f)
            } else {
                Triple(w, h, 0f)
            }

            // لو القطعة مش هتتناسب في الصف الحالي — نزل لصف جديد
            if (currentX + finalW > fabricWidth - gap) {
                currentX = gap
                currentY += rowHeight + gap
                rowHeight = 0f
            }

            placed.add(
                PlacedPiece(
                    piece    = piece,
                    x        = currentX,
                    y        = currentY,
                    rotation = rotation
                )
            )

            currentX += finalW + gap
            if (finalH > rowHeight) rowHeight = finalH
        }

        val totalLength = currentY + rowHeight + gap

        return NestingLayout(
            fabricWidth   = fabricWidth,
            fabricLength  = totalLength,
            placedPieces  = placed
        )
    }
}
