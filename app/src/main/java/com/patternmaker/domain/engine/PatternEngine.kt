package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

object PatternEngine {

    fun generateTrouser(
        m: Measurements,
        model: TrouserModel
    ): List<PatternPiece> {
        val pieces = mutableListOf<PatternPiece>()
        pieces.add(frontPanel(m, model))
        pieces.add(backPanel(m, model))
        pieces.add(waistband(m, model))
        if (model.hasLegBand) pieces.add(legBand(m))
        return pieces
    }

    // ── الأمامية ──────────────────────────────────────────────
    private fun frontPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.frontWidth(m, model) + m.seamAllowance * 2
        val h  = m.length + m.seamAllowance * 2
        val cd = FormulaEngine.frontCrotchDepth(m, model)
        val bw = FormulaEngine.bottomLegWidth(m, model)

        val points = listOf(
            Point(0f, 0f),
            Point(w, 0f),
            Point(w, h),
            Point((w - bw) / 2f, h),
            Point(0f, cd),
        )

        val crotchCurve = BezierCurve(
            start    = Point(0f, cd),
            control1 = Point(0f, cd * 0.5f),
            control2 = Point(w * 0.3f, 0f),
            end      = Point(w, 0f)
        )

        return PatternPiece(
            id      = "front_panel",
            nameAr  = "الأمامية",
            points  = points,
            curves  = listOf(crotchCurve),
            quantity = 2,
            widthCm  = w,
            heightCm = h + cd
        )
    }

    // ── الخلفية ───────────────────────────────────────────────
    private fun backPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.backWidth(m, model) + m.seamAllowance * 2
        val h  = m.length + m.seamAllowance * 2
        val cd = FormulaEngine.backCrotchDepth(m, model)
        val bw = FormulaEngine.bottomLegWidth(m, model) + 2f

        val points = listOf(
            Point(0f, 0f),
            Point(w, 0f),
            Point(w, h),
            Point((w - bw) / 2f, h),
            Point(0f, cd),
        )

        val crotchCurve = BezierCurve(
            start    = Point(0f, cd),
            control1 = Point(0f, cd * 0.4f),
            control2 = Point(w * 0.4f, 0f),
            end      = Point(w, 0f)
        )

        return PatternPiece(
            id       = "back_panel",
            nameAr   = "الخلفية",
            points   = points,
            curves   = listOf(crotchCurve),
            quantity = 2,
            widthCm  = w,
            heightCm = h + cd
        )
    }

    // ── الخصر ─────────────────────────────────────────────────
    private fun waistband(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.waistbandLength(m)
        val h = FormulaEngine.waistbandWidth()

        return PatternPiece(
            id       = "waistband",
            nameAr   = "الخصر",
            points   = rectPoints(w, h),
            quantity = 1,
            widthCm  = w,
            heightCm = h
        )
    }

    // ── كمة الساق ─────────────────────────────────────────────
    private fun legBand(m: Measurements): PatternPiece {
        val w = FormulaEngine.legBandLength(m)
        val h = FormulaEngine.legBandWidth()

        return PatternPiece(
            id       = "leg_band",
            nameAr   = "كمة الساق",
            points   = rectPoints(w, h),
            quantity = 2,
            widthCm  = w,
            heightCm = h
        )
    }

    // ── مساعد: مستطيل ─────────────────────────────────────────
    private fun rectPoints(w: Float, h: Float) = listOf(
        Point(0f, 0f),
        Point(w,  0f),
        Point(w,  h),
        Point(0f, h)
    )
}
