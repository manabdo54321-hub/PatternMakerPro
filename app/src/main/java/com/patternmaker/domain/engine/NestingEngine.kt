package com.patternmaker.domain.engine

import com.patternmaker.domain.model.NestingLayout
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.PlacedPiece
import kotlin.math.min

/**
 * NestingEngine — خوارزمية Bottom-Left Fill المحسّنة
 * مع دعم التدوير وحساب الهدر الدقيق
 */
object NestingEngine {

    fun autoNest(
        pieces: List<PatternPiece>,
        fabricWidth: Float,
        allowRotation: Boolean = true,
        gap: Float = 1.5f  // مسافة أمان بين القطع = هامش الخياطة
    ): NestingLayout {

        val placed = mutableListOf<PlacedPiece>()

        // توسيع القطع حسب الكمية — وترتيبها من الأكبر للأصغر
        val allPieces = pieces
            .flatMap { piece -> List(piece.quantity) { piece } }
            .sortedByDescending { it.widthCm * it.heightCm }

        // شبكة المواضع الفارغة (Skyline)
        val skyline = FloatArray(fabricWidth.toInt() + 1) { 0f }

        for (piece in allPieces) {
            val (bestX, bestY, bestRotation) = findBestPosition(
                piece, skyline, fabricWidth, allowRotation, gap
            )

            val finalW = if (bestRotation == 90f) piece.heightCm else piece.widthCm
            val finalH = if (bestRotation == 90f) piece.widthCm  else piece.heightCm

            placed.add(PlacedPiece(piece, bestX, bestY, bestRotation))

            // تحديث الـ Skyline
            val startX = bestX.toInt().coerceAtLeast(0)
            val endX   = (bestX + finalW + gap).toInt().coerceAtMost(skyline.size - 1)
            for (x in startX..endX) {
                skyline[x] = maxOf(skyline[x], bestY + finalH + gap)
            }
        }

        val totalLength = placed.maxOfOrNull { it.y + it.piece.heightCm } ?: 0f

        return NestingLayout(
            fabricWidth  = fabricWidth,
            fabricLength = totalLength + gap,
            placedPieces = placed
        )
    }

    // ══════════════════════════════════════════════════════════
    // إيجاد أفضل موضع للقطعة (Bottom-Left + Skyline)
    // ══════════════════════════════════════════════════════════
    private fun findBestPosition(
        piece: PatternPiece,
        skyline: FloatArray,
        fabricWidth: Float,
        allowRotation: Boolean,
        gap: Float
    ): Triple<Float, Float, Float> {

        var bestX   = 0f
        var bestY   = Float.MAX_VALUE
        var bestRot = 0f

        val rotations = if (allowRotation) listOf(0f, 90f) else listOf(0f)

        for (rotation in rotations) {
            val w = if (rotation == 90f) piece.heightCm else piece.widthCm
            val h = if (rotation == 90f) piece.widthCm  else piece.heightCm

            if (w > fabricWidth - gap) continue

            // جرب كل موضع أفقي ممكن
            var x = gap
            while (x + w <= fabricWidth - gap) {
                val y = getLowestY(skyline, x, w)
                if (y < bestY) {
                    bestY   = y
                    bestX   = x
                    bestRot = rotation
                }
                x += 0.5f  // دقة نصف سنتيمتر
            }
        }

        return Triple(bestX, if (bestY == Float.MAX_VALUE) 0f else bestY, bestRot)
    }

    // أعلى نقطة في الـ Skyline لمنطقة معينة
    private fun getLowestY(skyline: FloatArray, x: Float, width: Float): Float {
        val start = x.toInt().coerceAtLeast(0)
        val end   = (x + width).toInt().coerceAtMost(skyline.size - 1)
        var maxY  = 0f
        for (i in start..end) {
            if (skyline[i] > maxY) maxY = skyline[i]
        }
        return maxY
    }

    // ══════════════════════════════════════════════════════════
    // حساب نسبة الهدر الدقيقة
    // ══════════════════════════════════════════════════════════
    fun calculateWaste(layout: NestingLayout): WasteReport {
        val fabricArea = layout.fabricWidth * layout.fabricLength
        val usedArea   = layout.placedPieces.sumOf {
            (it.piece.widthCm * it.piece.heightCm).toDouble()
        }.toFloat()
        val wasteArea   = fabricArea - usedArea
        val wastePercent = if (fabricArea > 0) (wasteArea / fabricArea) * 100f else 0f

        return WasteReport(
            fabricArea   = fabricArea,
            usedArea     = usedArea,
            wasteArea    = wasteArea,
            wastePercent = wastePercent,
            fabricLength = layout.fabricLength,
            fabricWidth  = layout.fabricWidth
        )
    }
}

data class WasteReport(
    val fabricArea:   Float,
    val usedArea:     Float,
    val wasteArea:    Float,
    val wastePercent: Float,
    val fabricLength: Float,
    val fabricWidth:  Float
)
