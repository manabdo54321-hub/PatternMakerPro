package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*
import kotlin.math.ceil

/**
 * NestingEngine الاحترافي
 * خوارزمية Skyline Bottom-Left مع دعم:
 * - عدة أطقم مقاسات
 * - طول ثابت أو أوتوماتيك
 * - احترام اتجاه الخامة
 * - تقرير هدر تفصيلي
 */
object NestingEngine {

    fun autoNest(
        pieces: List<PatternPiece>,
        settings: FabricSettings
    ): NestingResult {

        val allPieces = expandPieces(pieces, settings.quantity)
        val placed    = mutableListOf<PlacedPiece>()
        val skyline   = FloatArray(ceil(settings.width).toInt() + 1) { 0f }

        for (piece in allPieces) {
            val best = findBestPosition(piece, skyline, settings)
            val finalW = if (best.rotation == 90f) piece.heightCm else piece.widthCm
            val finalH = if (best.rotation == 90f) piece.widthCm  else piece.heightCm

            // تحقق من حد الطول لو محدد
            if (settings.length != null && best.y + finalH > settings.length) {
                continue  // القطعة مش بتتناسب في الطول المحدد
            }

            placed.add(PlacedPiece(piece, best.x, best.y, best.rotation))
            updateSkyline(skyline, best.x, finalW, best.y + finalH + settings.gap)
        }

        val autoLength = placed.maxOfOrNull { p ->
            val h = if (p.rotation == 90f) p.piece.widthCm else p.piece.heightCm
            p.y + h
        }?.plus(settings.gap) ?: 0f

        val fabricLength = settings.length ?: autoLength

        val layout = NestingLayout(
            fabricWidth  = settings.width,
            fabricLength = fabricLength,
            placedPieces = placed,
            settings     = settings
        )

        return NestingResult(
            layout  = layout,
            report  = buildReport(layout, pieces, settings, placed.size, allPieces.size)
        )
    }

    // ── توسيع القطع حسب الكمية والأطقم ──────────────────────
    private fun expandPieces(
        pieces: List<PatternPiece>,
        quantity: Int
    ): List<PatternPiece> =
        (1..quantity).flatMap { _ ->
            pieces.flatMap { piece -> List(piece.quantity) { piece } }
        }.sortedByDescending { it.widthCm * it.heightCm }

    // ── إيجاد أفضل موضع (Skyline BL) ─────────────────────────
    private fun findBestPosition(
        piece: PatternPiece,
        skyline: FloatArray,
        settings: FabricSettings
    ): BestPosition {
        var bestX   = 0f
        var bestY   = Float.MAX_VALUE
        var bestRot = 0f

        val rotations = when {
            !settings.allowRotation           -> listOf(0f)
            settings.respectGrainLine         -> listOf(0f, 180f)  // اتجاه الخامة فقط
            else                              -> listOf(0f, 90f, 180f, 270f)
        }

        for (rot in rotations) {
            val w = if (rot == 90f || rot == 270f) piece.heightCm else piece.widthCm
            val h = if (rot == 90f || rot == 270f) piece.widthCm  else piece.heightCm

            if (w + settings.gap * 2 > settings.width) continue

            var x = settings.gap
            while (x + w <= settings.width - settings.gap) {
                val y = getSkylineHeight(skyline, x, w)
                if (y < bestY) {
                    bestY   = y
                    bestX   = x
                    bestRot = rot
                }
                x += 0.5f
            }
        }

        return BestPosition(bestX, if (bestY == Float.MAX_VALUE) settings.gap else bestY, bestRot)
    }

    private fun getSkylineHeight(skyline: FloatArray, x: Float, width: Float): Float {
        val start = x.toInt().coerceAtLeast(0)
        val end   = (x + width).toInt().coerceAtMost(skyline.size - 1)
        var maxH  = 0f
        for (i in start..end) if (skyline[i] > maxH) maxH = skyline[i]
        return maxH
    }

    private fun updateSkyline(skyline: FloatArray, x: Float, width: Float, height: Float) {
        val start = x.toInt().coerceAtLeast(0)
        val end   = (x + width).toInt().coerceAtMost(skyline.size - 1)
        for (i in start..end) if (height > skyline[i]) skyline[i] = height
    }

    // ── تقرير الهدر التفصيلي ─────────────────────────────────
    private fun buildReport(
        layout: NestingLayout,
        originalPieces: List<PatternPiece>,
        settings: FabricSettings,
        placedCount: Int,
        totalCount: Int
    ): NestingReport {
        val pieceBreakdown = originalPieces.map { piece ->
            val placed = layout.placedPieces.count { it.piece.id == piece.id }
            PieceCount(
                nameAr    = piece.nameAr,
                requested = piece.quantity * settings.quantity,
                placed    = placed
            )
        }

        return NestingReport(
            fabricWidth    = layout.fabricWidth,
            fabricLength   = layout.fabricLength,
            fabricArea     = layout.totalArea,
            usedArea       = layout.usedArea,
            wasteArea      = layout.totalArea - layout.usedArea,
            wastePercent   = layout.wastePercent,
            efficiency     = layout.efficiency,
            quantity       = settings.quantity,
            totalPieces    = totalCount,
            placedPieces   = placedCount,
            missingPieces  = totalCount - placedCount,
            pieceBreakdown = pieceBreakdown,
            fabricMeters   = layout.fabricLength / 100f
        )
    }

    private data class BestPosition(val x: Float, val y: Float, val rotation: Float)
}

// ── نتيجة التعشيق ─────────────────────────────────────────────
data class NestingResult(
    val layout: NestingLayout,
    val report: NestingReport
)

// ── تقرير الهدر ───────────────────────────────────────────────
data class NestingReport(
    val fabricWidth:    Float,
    val fabricLength:   Float,
    val fabricArea:     Float,
    val usedArea:       Float,
    val wasteArea:      Float,
    val wastePercent:   Float,
    val efficiency:     Float,
    val quantity:       Int,
    val totalPieces:    Int,
    val placedPieces:   Int,
    val missingPieces:  Int,
    val pieceBreakdown: List<PieceCount>,
    val fabricMeters:   Float   // الطول بالمتر للشراء
)

data class PieceCount(
    val nameAr:    String,
    val requested: Int,
    val placed:    Int
)
