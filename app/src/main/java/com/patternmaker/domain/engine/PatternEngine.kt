package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

object PatternEngine {

    fun generateTrouser(
        m: Measurements,
        model: TrouserModel,
        enabledPieces: List<OptionalPiece> = model.optionalPieces
            .filter { it.defaultEnabled }
    ): List<PatternPiece> = buildList {

        // القطع الأساسية
        add(frontPanel(m, model))
        add(backPanel(m, model))
        add(waistband(m, model))
        if (model.hasLegBand) add(legBand(m))

        // القطع الاختيارية
        if (enabledPieces.contains(OptionalPiece.FRONT_POCKET)) {
            add(frontPocketFacing(m, model))
            add(frontPocketBag(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_WELT_POCKET)) {
            add(backWeltPocket(m, model))
            add(backWeltPocketBag(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_YOKE)) {
            add(backYoke(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.FLY)) {
            add(fly(m, model))
            add(flyFacing(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.CARGO_POCKET)) {
            add(cargoPocket(m, model))
            add(cargoPocketFlap(m, model))
        }
    }

    // ══════════════════════════════════════════════════════════
    // القطع الأساسية
    // ══════════════════════════════════════════════════════════
    private fun frontPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val fw = FormulaEngine.frontWidth(m, model)
        val cd = FormulaEngine.frontCrotchDepth(m, model)
        val bw = FormulaEngine.bottomLegWidth(m, model)
        val sa = m.seamAllowance
        return PatternPiece(
            id = "front_panel", nameAr = "الأمامية",
            points = listOf(
                Point(sa, sa), Point(fw + sa, sa),
                Point(fw + sa, m.length + cd - sa),
                Point(bw + sa, m.length + cd - sa),
                Point(sa, cd + sa)
            ),
            curves = listOf(
                CurveEngine.frontCrotchCurve(cd, fw),
                CurveEngine.frontWaistCurve(fw)
            ),
            quantity = 2, widthCm = fw + sa * 2, heightCm = m.length + cd + sa * 2
        )
    }

    private fun backPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val bw  = FormulaEngine.backWidth(m, model)
        val cd  = FormulaEngine.backCrotchDepth(m, model)
        val blw = FormulaEngine.bottomLegWidth(m, model) + 2f
        val sa  = m.seamAllowance
        return PatternPiece(
            id = "back_panel", nameAr = "الخلفية",
            points = listOf(
                Point(sa, sa), Point(bw + sa, sa),
                Point(bw + sa, m.length + cd - sa),
                Point(blw + sa, m.length + cd - sa),
                Point(sa, cd + sa)
            ),
            curves = listOf(
                CurveEngine.backCrotchCurve(cd, bw),
                CurveEngine.backWaistCurve(bw)
            ),
            quantity = 2, widthCm = bw + sa * 2, heightCm = m.length + cd + sa * 2
        )
    }

    private fun waistband(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.waistbandLength(m)
        val h = FormulaEngine.waistbandWidth(model.waistbandType)
        return PatternPiece(
            id = "waistband",
            nameAr = "الكمر — ${model.waistbandType.nameAr}",
            points = rectPoints(w, h),
            quantity = 1, widthCm = w, heightCm = h
        )
    }

    private fun legBand(m: Measurements): PatternPiece {
        val w = FormulaEngine.legBandLength(m)
        return PatternPiece(
            id = "leg_band", nameAr = "كمة الساق",
            points = rectPoints(w, FormulaEngine.legBandWidth()),
            quantity = 2,
            widthCm = w, heightCm = FormulaEngine.legBandWidth()
        )
    }

    // ══════════════════════════════════════════════════════════
    // الجيب الأمامي
    // ══════════════════════════════════════════════════════════
    private fun frontPocketFacing(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.frontPocketWidth(m, model)
        val h = FormulaEngine.frontPocketHeight()
        // شكل شبه منحرف — أعلى أضيق
        return PatternPiece(
            id = "front_pocket_facing", nameAr = "واجهة الجيب الأمامي",
            points = listOf(
                Point(2f, 0f), Point(w - 2f, 0f),
                Point(w, h),   Point(0f, h)
            ),
            curves = listOf(
                BezierCurve(
                    start    = Point(w - 2f, 0f),
                    control1 = Point(w, h * 0.3f),
                    control2 = Point(w, h * 0.7f),
                    end      = Point(w, h)
                )
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun frontPocketBag(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.frontPocketBagWidth(m, model)
        val h = FormulaEngine.frontPocketBagHeight()
        return PatternPiece(
            id = "front_pocket_bag", nameAr = "كيس الجيب الأمامي",
            points = rectPoints(w, h),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    // ══════════════════════════════════════════════════════════
    // الجيب الخلفي (بطاقة)
    // ══════════════════════════════════════════════════════════
    private fun backWeltPocket(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.backWeltPocketWidth(m, model)
        val h = FormulaEngine.backWeltPocketHeight()
        return PatternPiece(
            id = "back_welt", nameAr = "بطاقة الجيب الخلفي",
            points = rectPoints(w, h),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun backWeltPocketBag(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.backWeltPocketBagWidth(m, model)
        val h = FormulaEngine.backWeltPocketBagHeight()
        return PatternPiece(
            id = "back_pocket_bag", nameAr = "كيس الجيب الخلفي",
            points = rectPoints(w, h),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    // ══════════════════════════════════════════════════════════
    // السفرة الخلفية (Back Yoke)
    // ══════════════════════════════════════════════════════════
    private fun backYoke(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.backYokeWidth(m, model)
        val h = FormulaEngine.backYokeHeight()
        // السفرة لها منحنى خفيف في الأسفل
        return PatternPiece(
            id = "back_yoke", nameAr = "السفرة الخلفية",
            points = listOf(
                Point(0f, 0f), Point(w, 0f),
                Point(w, h),   Point(0f, h)
            ),
            curves = listOf(
                BezierCurve(
                    start    = Point(0f, h),
                    control1 = Point(w * 0.25f, h + 1.5f),
                    control2 = Point(w * 0.75f, h + 1.5f),
                    end      = Point(w, h)
                )
            ),
            quantity = 1, widthCm = w, heightCm = h + 1.5f
        )
    }

    // ══════════════════════════════════════════════════════════
    // البتلتة (Fly)
    // ══════════════════════════════════════════════════════════
    private fun fly(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.flyWidth(m, model)
        val h = FormulaEngine.flyHeight(m, model)
        // البتلتة لها منحنى في الأسفل
        return PatternPiece(
            id = "fly", nameAr = "البتلتة",
            points = listOf(
                Point(0f, 0f), Point(w, 0f),
                Point(w, h * 0.8f), Point(0f, h)
            ),
            curves = listOf(
                BezierCurve(
                    start    = Point(w, h * 0.8f),
                    control1 = Point(w, h),
                    control2 = Point(w * 0.5f, h),
                    end      = Point(0f, h)
                )
            ),
            quantity = 1, widthCm = w, heightCm = h
        )
    }

    private fun flyFacing(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.flyFacingWidth(m, model)
        val h = FormulaEngine.flyHeight(m, model)
        return PatternPiece(
            id = "fly_facing", nameAr = "واجهة البتلتة",
            points = rectPoints(w, h),
            quantity = 1, widthCm = w, heightCm = h
        )
    }

    // ══════════════════════════════════════════════════════════
    // جيب الكارجو
    // ══════════════════════════════════════════════════════════
    private fun cargoPocket(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.cargoPocketPleatWidth(m, model)
        val h = FormulaEngine.cargoPocketHeight()
        return PatternPiece(
            id = "cargo_pocket", nameAr = "جيب الكارجو (مع البدّة)",
            points = rectPoints(w, h),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun cargoPocketFlap(m: Measurements, model: TrouserModel): PatternPiece {
        val w = FormulaEngine.cargoPocketFlapWidth(m, model)
        val h = FormulaEngine.cargoPocketFlapHeight()
        // الغطاء له زوايا مدورة في الأسفل
        return PatternPiece(
            id = "cargo_flap", nameAr = "غطاء جيب الكارجو",
            points = listOf(
                Point(0f, 0f), Point(w, 0f),
                Point(w, h - 1f), Point(w * 0.5f, h),
                Point(0f, h - 1f)
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun rectPoints(w: Float, h: Float) = listOf(
        Point(0f, 0f), Point(w, 0f),
        Point(w, h),   Point(0f, h)
    )
}
