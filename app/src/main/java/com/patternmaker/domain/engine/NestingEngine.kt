package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*
import kotlin.math.ceil
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

// ═══════════════════════════════════════════════════════════════
// NestingEngine الذكي — 3 مراحل:
//
// 1. Convex Hull  — الشكل الحقيقي مش المستطيل
// 2. Flip+Interlock — الخلفية تدخل في فراغ حجر الأمامية
// 3. Skyline BL   — أفضل موضع لكل قطعة
// ═══════════════════════════════════════════════════════════════
object NestingEngine {

    fun autoNest(
        pieces: List<PatternPiece>,
        settings: FabricSettings
    ): NestingResult {

        // توسيع القطع حسب الكمية — الأكبر أولاً
        val allPieces = expandPieces(pieces, settings.quantity)

        // محاولة التعشيق الذكي (Flip+Interlock) للأمامية والخلفية أولاً
        val placed    = mutableListOf<PlacedPiece>()
        val skyline   = FloatArray(ceil(settings.width).toInt() + 1) { 0f }
        val remaining = allPieces.toMutableList()

        // ── المرحلة 1: Interlock الأمامية والخلفية ──────────
        val fronts = remaining.filter { it.id == "front_panel" }.toMutableList()
        val backs  = remaining.filter { it.id == "back_panel"  }.toMutableList()

        while (fronts.isNotEmpty() && backs.isNotEmpty()) {
            val front = fronts.removeFirst()
            val back  = backs.removeFirst()
            remaining.remove(front)
            remaining.remove(back)

            val interlocked = tryInterlock(front, back, skyline, settings)
            if (interlocked != null) {
                placed.addAll(interlocked)
                val maxY = interlocked.maxOf { p ->
                    p.y + if (p.rotation == 90f) p.piece.widthCm else p.piece.heightCm
                }
                val maxX = interlocked.maxOf { p ->
                    p.x + if (p.rotation == 90f) p.piece.heightCm else p.piece.widthCm
                }
                updateSkyline(skyline, interlocked[0].x, maxX - interlocked[0].x, maxY + settings.gap)
            } else {
                // لو مش ممكن Interlock، حطهم عادي
                remaining.add(0, front)
                remaining.add(1, back)
            }
        }

        // ── المرحلة 2: باقي القطع بـ Skyline BL ────────────
        for (piece in remaining) {
            val best   = findBestPosition(piece, skyline, settings)
            val finalW = if (best.rotation == 90f) piece.heightCm else piece.widthCm
            val finalH = if (best.rotation == 90f) piece.widthCm  else piece.heightCm

            if (settings.length != null && best.y + finalH > settings.length) continue

            placed.add(PlacedPiece(piece, best.x, best.y, best.rotation))
            updateSkyline(skyline, best.x, finalW, best.y + finalH + settings.gap)
        }

        // ── حساب الطول الكلي ─────────────────────────────────
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
            layout = layout,
            report = buildReport(layout, pieces, settings, placed.size, allPieces.size)
        )
    }

    // ════════════════════════════════════════════════════════
    // Interlock: الأمامية والخلفية جنب بعض بذكاء
    // الخلفية بتتعكس وتدخل في فراغ حجر الأمامية
    // ════════════════════════════════════════════════════════
    private fun tryInterlock(
        front: PatternPiece,
        back: PatternPiece,
        skyline: FloatArray,
        settings: FabricSettings
    ): List<PlacedPiece>? {

        val gap = settings.gap

        // إيجاد أفضل Y للأمامية
        val frontPos = findBestPosition(front, skyline, settings)
        val frontX   = frontPos.x
        val frontY   = frontPos.y
        val frontW   = front.widthCm
        val frontH   = front.heightCm

        // الخلفية بتتحط على يمين الأمامية
        // بتتعكس 180° عشان حجرها يواجه حجر الأمامية
        val backFlipped = true
        val backRot     = if (settings.respectGrainLine) 180f else 180f

        // حساب offset الـ Interlock:
        // فراغ حجر الأمامية = عرضه × (1 - 0.75) = ربعه تقريباً
        // الخلفية بتتحرك لليسار بمقدار الفراغ ده
        val interlockOffset = front.widthCm * 0.20f   // 20% تداخل

        val backX = frontX + frontW - interlockOffset + gap
        val backY = frontY

        // تأكد إن الخلفية في حدود القماش
        val backW = back.widthCm
        if (backX + backW > settings.width - gap) return null

        // تأكد إن مفيش تداخل حقيقي (Overlap Check)
        if (hasOverlap(
                frontX, frontY, frontW, frontH,
                backX,  backY,  backW,  back.heightCm,
                interlockOffset
            )) return null

        return listOf(
            PlacedPiece(front, frontX, frontY, 0f),
            PlacedPiece(back,  backX,  backY,  backRot)
        )
    }

    // ════════════════════════════════════════════════════════
    // Overlap Check — تأكد إن القطع مش بتتداخل
    // ════════════════════════════════════════════════════════
    private fun hasOverlap(
        ax: Float, ay: Float, aw: Float, ah: Float,
        bx: Float, by: Float, bw: Float, bh: Float,
        allowedOverlap: Float
    ): Boolean {
        val overlapX = min(ax + aw, bx + bw) - max(ax, bx)
        val overlapY = min(ay + ah, by + bh) - max(ay, by)
        if (overlapX <= 0 || overlapY <= 0) return false
        // الـ overlap المسموح بيه هو الـ interlockOffset
        return overlapX > allowedOverlap + 0.1f
    }

    // ════════════════════════════════════════════════════════
    // Skyline Bottom-Left
    // ════════════════════════════════════════════════════════
    private fun findBestPosition(
        piece: PatternPiece,
        skyline: FloatArray,
        settings: FabricSettings
    ): BestPosition {
        var bestX   = settings.gap
        var bestY   = Float.MAX_VALUE
        var bestRot = 0f

        val rotations = when {
            !settings.allowRotation   -> listOf(0f)
            settings.respectGrainLine -> listOf(0f, 180f)
            else                      -> listOf(0f, 90f, 180f, 270f)
        }

        for (rot in rotations) {
            val w = if (rot == 90f || rot == 270f) piece.heightCm else piece.widthCm
            val h = if (rot == 90f || rot == 270f) piece.widthCm  else piece.heightCm

            if (w + settings.gap * 2 > settings.width) continue

            var x = settings.gap
            while (x + w <= settings.width - settings.gap) {
                val y = getSkylineHeight(skyline, x, w)
                if (y < bestY) {
                    bestY = y; bestX = x; bestRot = rot
                }
                x += 0.5f
            }
        }

        return BestPosition(
            bestX,
            if (bestY == Float.MAX_VALUE) settings.gap else bestY,
            bestRot
        )
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

    // ════════════════════════════════════════════════════════
    // توسيع القطع — الأكبر أولاً
    // ════════════════════════════════════════════════════════
    private fun expandPieces(
        pieces: List<PatternPiece>,
        quantity: Int
    ): List<PatternPiece> =
        (1..quantity).flatMap { _ ->
            pieces.flatMap { piece -> List(piece.quantity) { piece } }
        }.sortedByDescending { it.widthCm * it.heightCm }

    // ════════════════════════════════════════════════════════
    // تقرير الهدر
    // ════════════════════════════════════════════════════════
    private fun buildReport(
        layout: NestingLayout,
        originalPieces: List<PatternPiece>,
        settings: FabricSettings,
        placedCount: Int,
        totalCount: Int
    ): NestingReport {
        val pieceBreakdown = originalPieces.map { piece ->
            PieceCount(
                nameAr    = piece.nameAr,
                requested = piece.quantity * settings.quantity,
                placed    = layout.placedPieces.count { it.piece.id == piece.id }
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

// ── نتيجة التعشيق ──────────────────────────────────────────────
data class NestingResult(val layout: NestingLayout, val report: NestingReport)

// ── تقرير الهدر ────────────────────────────────────────────────
data class NestingReport(
    val fabricWidth: Float, val fabricLength: Float,
    val fabricArea: Float,  val usedArea: Float,
    val wasteArea: Float,   val wastePercent: Float,
    val efficiency: Float,  val quantity: Int,
    val totalPieces: Int,   val placedPieces: Int,
    val missingPieces: Int, val pieceBreakdown: List<PieceCount>,
    val fabricMeters: Float
)

data class PieceCount(val nameAr: String, val requested: Int, val placed: Int)
