package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

object PatternEngine {

    fun generateTrouser(
        m: Measurements,
        model: TrouserModel
    ): List<PatternPiece> = buildList {
        add(frontPanel(m, model))
        add(backPanel(m, model))
        add(waistband(m))
        if (model.hasLegBand) add(legBand(m))
    }

    // ══════════════════════════════════════════════════════════
    // الأمامية (Front Panel)
    // ══════════════════════════════════════════════════════════
    private fun frontPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val fw  = FormulaEngine.frontWidth(m, model)
        val cd  = FormulaEngine.frontCrotchDepth(m, model)
        val bw  = FormulaEngine.bottomLegWidth(m, model)
        val sa  = m.seamAllowance
        val len = m.length

        // منحنيات دقيقة من CurveEngine
        val crotchCurve = CurveEngine.frontCrotchCurve(cd, fw)
        val waistCurve  = CurveEngine.frontWaistCurve(fw)
        val innerLeg    = CurveEngine.innerLegCurve(len, cd, fw * 0.3f, bw * 0.4f)

        // نقاط الشكل الكامل (مع هامش الخياطة)
        val points = listOf(
            Point(sa,       sa),                    // أعلى يسار
            Point(fw + sa,  sa),                    // أعلى يمين
            Point(fw + sa,  len + cd - sa),         // أسفل يمين
            Point(bw + sa,  len + cd - sa),         // أسفل الساق
            Point(sa,       cd + sa),               // الكيلوت
        )

        return PatternPiece(
            id       = "front_panel",
            nameAr   = "الأمامية",
            points   = points,
            curves   = listOf(crotchCurve, waistCurve, innerLeg),
            quantity = 2,
            widthCm  = fw + sa * 2,
            heightCm = len + cd + sa * 2
        )
    }

    // ══════════════════════════════════════════════════════════
    // الخلفية (Back Panel)
    // ══════════════════════════════════════════════════════════
    private fun backPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val bw  = FormulaEngine.backWidth(m, model)
        val cd  = FormulaEngine.backCrotchDepth(m, model)
        val blw = FormulaEngine.bottomLegWidth(m, model) + 2f
        val sa  = m.seamAllowance
        val len = m.length

        val crotchCurve = CurveEngine.backCrotchCurve(cd, bw)
        val waistCurve  = CurveEngine.backWaistCurve(bw)
        val innerLeg    = CurveEngine.innerLegCurve(len, cd, bw * 0.3f, blw * 0.4f)

        val points = listOf(
            Point(sa,       sa),
            Point(bw + sa,  sa),
            Point(bw + sa,  len + cd - sa),
            Point(blw + sa, len + cd - sa),
            Point(sa,       cd + sa),
        )

        return PatternPiece(
            id       = "back_panel",
            nameAr   = "الخلفية",
            points   = points,
            curves   = listOf(crotchCurve, waistCurve, innerLeg),
            quantity = 2,
            widthCm  = bw + sa * 2,
            heightCm = len + cd + sa * 2
        )
    }

    // ══════════════════════════════════════════════════════════
    // الخصر والكمّة — مستطيلات بسيطة
    // ══════════════════════════════════════════════════════════
    private fun waistband(m: Measurements): PatternPiece {
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

    private fun rectPoints(w: Float, h: Float) = listOf(
        Point(0f, 0f), Point(w, 0f),
        Point(w, h),   Point(0f, h)
    )
}
