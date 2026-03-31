package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

object PatternEngine {

    fun generateTrouser(
        m: Measurements,
        model: TrouserModel,
        enabledPieces: List<OptionalPiece> = model.optionalPieces.filter { it.defaultEnabled }
    ): List<PatternPiece> = buildList {
        add(frontPanel(m, model))
        add(backPanel(m, model))
        add(waistband(m, model))
        if (model.hasLegBand) add(legBand(m))
        if (enabledPieces.contains(OptionalPiece.FRONT_POCKET)) {
            add(frontPocketFacing(m, model))
            add(frontPocketBag(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_WELT_POCKET)) {
            add(backWeltPocket(m, model))
            add(backWeltPocketBag(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_YOKE)) add(backYoke(m, model))
        if (enabledPieces.contains(OptionalPiece.FLY)) {
            add(fly(m, model))
            add(flyFacing(m, model))
        }
        if (enabledPieces.contains(OptionalPiece.CARGO_POCKET)) {
            add(cargoPocket(m, model))
            add(cargoPocketFlap(m, model))
        }
    }

    private fun frontPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val fw = FormulaEngine.frontWidth(m, model)
        val cd = FormulaEngine.frontCrotchDepth(m, model)
        val sa = m.seamAllowance
        val wd = fw * 0.015f
        val p0 = Point(sa, sa + wd)
        val p5 = Point(sa + fw * 0.12f, sa + cd)
        val segments = mutableListOf<PathSegment>()
        segments.add(CurveEngine.frontWaistCurve(p0, fw, wd))
        segments.add(PathSegment.LineTo(Point(fw + sa, sa + cd + m.length)))
        segments.add(PathSegment.LineTo(Point(FormulaEngine.bottomLegWidth(m, model) * 0.3f + sa, sa + cd + m.length)))
        segments.add(PathSegment.LineTo(p5))
        segments.add(CurveEngine.frontCrotchCurve(p5, cd - wd, fw * 0.88f))
        return PatternPiece(
            id = "front_panel", nameAr = "الأمامية",
            startPoint = p0, segments = segments,
            grainLineAngle = 90f, seamAllowance = sa,
            quantity = 2, widthCm = fw + sa * 2f, heightCm = cd + m.length + sa * 2f
        )
    }

    private fun backPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val bw  = FormulaEngine.backWidth(m, model)
        val cd  = FormulaEngine.backCrotchDepth(m, model)
        val blw = FormulaEngine.bottomLegWidth(m, model) + 2f
        val sa  = m.seamAllowance
        val wr  = bw * 0.03f
        val p0  = Point(sa, sa)
        val p5  = Point(bw * 0.12f + sa, sa + cd)
        val segments = mutableListOf<PathSegment>()
        segments.add(CurveEngine.backWaistCurve(p0, bw, wr))
        segments.add(PathSegment.LineTo(Point(bw + sa, sa + cd + m.length)))
        segments.add(PathSegment.LineTo(Point(blw * 0.3f + sa, sa + cd + m.length)))
        segments.add(PathSegment.LineTo(p5))
        segments.add(CurveEngine.backCrotchCurve(p5, cd - wr, bw * 0.88f))
        return PatternPiece(
            id = "back_panel", nameAr = "الخلفية",
            startPoint = p0, segments = segments,
            grainLineAngle = 90f, seamAllowance = sa,
            quantity = 2, widthCm = bw + sa * 2f, heightCm = cd + m.length + sa * 2f
        )
    }

    private fun waistband(m: Measurements, model: TrouserModel) =
        rectPiece(
            "waistband", "الكمر — ${model.waistbandType.nameAr}",
            FormulaEngine.waistbandLength(m),
            FormulaEngine.waistbandWidth(model.waistbandType), 1
        )

    private fun legBand(m: Measurements) =
        rectPiece(
            "leg_band", "كمة الساق",
            FormulaEngine.legBandLength(m),
            FormulaEngine.legBandWidth(), 2
        )

    private fun frontPocketFacing(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.frontPocketWidth(m, model)
        val h  = FormulaEngine.frontPocketHeight()
        val p0 = Point(2f, 0f)
        return PatternPiece(
            id = "front_pocket_facing", nameAr = "واجهة الجيب الأمامي",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w - 2f, 0f)),
                PathSegment.CurveTo(Point(w, h * 0.25f), Point(w, h * 0.75f), Point(w, h)),
                PathSegment.LineTo(Point(0f, h)),
                PathSegment.LineTo(p0)
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun frontPocketBag(m: Measurements, model: TrouserModel) =
        rectPiece(
            "front_pocket_bag", "كيس الجيب الأمامي",
            FormulaEngine.frontPocketBagWidth(m, model),
            FormulaEngine.frontPocketBagHeight(), 2
        )

    private fun backWeltPocket(m: Measurements, model: TrouserModel) =
        rectPiece(
            "back_welt", "بطاقة الجيب الخلفي",
            FormulaEngine.backWeltPocketWidth(m, model),
            FormulaEngine.backWeltPocketHeight(), 2
        )

    private fun backWeltPocketBag(m: Measurements, model: TrouserModel) =
        rectPiece(
            "back_pocket_bag", "كيس الجيب الخلفي",
            FormulaEngine.backWeltPocketBagWidth(m, model),
            FormulaEngine.backWeltPocketBagHeight(), 2
        )

    private fun backYoke(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.backYokeWidth(m, model)
        val h  = FormulaEngine.backYokeHeight()
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "back_yoke", nameAr = "السفرة الخلفية",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h)),
                PathSegment.CurveTo(Point(w * 0.75f, h + 1.5f), Point(w * 0.25f, h + 1.5f), Point(0f, h)),
                PathSegment.LineTo(p0)
            ),
            quantity = 1, widthCm = w, heightCm = h + 1.5f
        )
    }

    private fun fly(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.flyWidth(m, model)
        val h  = FormulaEngine.flyHeight(m, model)
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "fly", nameAr = "البتلتة",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h * 0.75f)),
                PathSegment.CurveTo(Point(w, h), Point(w * 0.5f, h), Point(0f, h)),
                PathSegment.LineTo(p0)
            ),
            quantity = 1, widthCm = w, heightCm = h
        )
    }

    private fun flyFacing(m: Measurements, model: TrouserModel) =
        rectPiece(
            "fly_facing", "واجهة البتلتة",
            FormulaEngine.flyFacingWidth(m, model),
            FormulaEngine.flyHeight(m, model), 1
        )

    private fun cargoPocket(m: Measurements, model: TrouserModel) =
        rectPiece(
            "cargo_pocket", "جيب الكارجو",
            FormulaEngine.cargoPocketPleatWidth(m, model),
            FormulaEngine.cargoPocketHeight(), 2
        )

    private fun cargoPocketFlap(m: Measurements, model: TrouserModel): PatternPiece {
        val w  = FormulaEngine.cargoPocketFlapWidth(m, model)
        val h  = FormulaEngine.cargoPocketFlapHeight()
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "cargo_flap", nameAr = "غطاء جيب الكارجو",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h - 1f)),
                PathSegment.CurveTo(Point(w, h), Point(w * 0.6f, h), Point(w * 0.5f, h)),
                PathSegment.CurveTo(Point(w * 0.4f, h), Point(0f, h), Point(0f, h - 1f)),
                PathSegment.LineTo(p0)
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun rectPiece(id: String, nameAr: String, w: Float, h: Float, qty: Int): PatternPiece {
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = id, nameAr = nameAr, startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h)),
                PathSegment.LineTo(Point(0f, h)),
                PathSegment.LineTo(p0)
            ),
            quantity = qty, widthCm = w, heightCm = h
        )
    }
}
