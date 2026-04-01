package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

// ═══════════════════════════════════════════════════════════════
// PatternEngine — نظام Anchor Points الاحترافي
//
// كل قطعة بتتبنى من نقاط ثابتة (Anchor Points) محسوبة
// بالمعادلات الحقيقية — لما تتغير المقاسات، كل النقاط
// بتتحرك تلقائياً وشكل الباترون بيتغير معاها
//
// نظام الإحداثيات:
//   (0,0) = أعلى يسار كل قطعة
//   X     = اتجاه العرض (يمين)
//   Y     = اتجاه الطول (أسفل)
// ═══════════════════════════════════════════════════════════════
object PatternEngine {

    fun generateTrouser(
        m: Measurements,
        model: TrouserModel,
        enabledPieces: List<OptionalPiece> = model.optionalPieces
            .filter { it.defaultEnabled }
    ): List<PatternPiece> = buildList {
        add(frontPanel(m, model))
        add(backPanel(m, model))
        add(waistband(m, model))
        if (model.hasLegBand) add(legBand(m))
        if (enabledPieces.contains(OptionalPiece.FRONT_POCKET)) {
            add(frontPocketFacing(m))
            add(frontPocketBag(m))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_WELT_POCKET)) {
            add(backWeltPocket(m))
            add(backWeltPocketBag(m))
        }
        if (enabledPieces.contains(OptionalPiece.BACK_YOKE)) add(backYoke(m))
        if (enabledPieces.contains(OptionalPiece.FLY)) {
            add(fly(m))
            add(flyFacing(m))
        }
        if (enabledPieces.contains(OptionalPiece.CARGO_POCKET)) {
            add(cargoPocket(m))
            add(cargoPocketFlap(m))
        }
    }

    // ════════════════════════════════════════════════════════
    // الأمامية — Front Panel
    //
    // Anchor Points:
    //   A = أعلى يسار (وسط الخصر الأمامي)
    //   B = أعلى يمين (جنب الخصر)
    //   C = يمين عند خط الورك (أعرض نقطة جانبية)
    //   D = أسفل يمين (جنب الكمة)
    //   E = أسفل يسار (داخل الكمة)
    //   F = نقطة الحجر (أقصى امتداد للحجر)
    //   G = عند خط الورك داخلياً
    //   H = أعلى يسار مرة تانية (إغلاق المسار)
    //
    //        A────────────B
    //        │             \  ← منحنى الخصر
    //        │              C
    //        │              │
    //        G              D
    //        │              │
    //        F──────────────E  ← خط الكمة
    //        ↑
    //    منحنى الحجر
    // ════════════════════════════════════════════════════════
    private fun frontPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val sa  = m.seamAllowance
        val cd  = FormulaEngine.crotchDepth(m)       // عمق الحجر
        val hd  = FormulaEngine.hipDepth(m)           // عمق الورك
        val fw  = FormulaEngine.frontHipWidth(m)      // عرض الأرداف الأمامي
        val ww  = FormulaEngine.frontWaistWidth(m)    // عرض الخصر الأمامي
        val ce  = FormulaEngine.frontCrotchExtension(m) // امتداد الحجر
        val hw  = FormulaEngine.frontHemWidth(m, model) // عرض الكمة
        val len = m.length                             // طول الساق

        // ── Anchor Points ─────────────────────────────────
        // A: أعلى يسار — بداية خط الخصر الأمامي
        val A = Point(sa, sa)

        // B: أعلى يمين — طرف الخصر عند الجنب
        // الجنب أعلى من المنتصف بـ 0.5 سم
        val B = Point(sa + ww, sa - 0.5f)

        // C: الجنب عند خط الورك — أعرض نقطة
        val C = Point(sa + fw, sa + hd)

        // D: الجنب أسفل — بداية الساق الخارجية
        val D = Point(sa + fw, sa + cd + len)

        // E: الكمة داخلياً
        val E = Point(sa + hw, sa + cd + len)

        // F: نقطة الحجر — أقصى امتداد
        // بتنزل من خط القعدة بـ 1 سم للأسفل (Crotch Drop)
        val F = Point(sa - ce, sa + cd + 1f)

        // G: الداخل عند خط الورك
        val G = Point(sa, sa + hd)

        // ── نقطة تحكم الحجر (45°) ─────────────────────────
        // القانون الاحترافي: نقطة التحكم على بعد 45° من F
        // بتعمل الانحناء الطبيعي للحجر
        val crotchCtrl = Point(
            x = sa - ce * 0.7f,
            y = sa + cd - ce * 0.3f
        )

        // ── بناء المسار ───────────────────────────────────
        val segments = mutableListOf<PathSegment>()

        // 1. خط الخصر: A → B (منحنى خفيف)
        segments.add(PathSegment.CurveTo(
            control1 = Point(A.x + ww * 0.4f, A.y - 0.3f),
            control2 = Point(A.x + ww * 0.8f, B.y - 0.2f),
            end      = B
        ))

        // 2. الجنب من الخصر للورك: B → C (منحنى الورك)
        segments.add(PathSegment.CurveTo(
            control1 = Point(B.x + 0.5f, B.y + hd * 0.4f),
            control2 = Point(C.x,        C.y - hd * 0.3f),
            end      = C
        ))

        // 3. الجنب من الورك للكمة: C → D (خط مستقيم)
        segments.add(PathSegment.LineTo(D))

        // 4. خط الكمة: D → E (مستقيم)
        segments.add(PathSegment.LineTo(E))

        // 5. الساق الداخلي: E → F (منحنى خفيف للداخل)
        segments.add(PathSegment.CurveTo(
            control1 = Point(E.x - (E.x - F.x) * 0.2f, E.y - len * 0.3f),
            control2 = Point(F.x + ce * 0.3f,           F.y + (E.y - F.y) * 0.3f),
            end      = F
        ))

        // 6. منحنى الحجر الأمامي: F → A
        // أهم منحنى في الباترون — بيحدد شكل الحجر
        segments.add(PathSegment.CurveTo(
            control1 = crotchCtrl,
            control2 = Point(A.x + ce * 0.1f, A.y + cd * 0.3f),
            end      = A
        ))

        val totalW = fw + ce + sa * 2f
        val totalH = cd + len + sa * 2f

        return PatternPiece(
            id             = "front_panel",
            nameAr         = "الأمامية",
            startPoint     = A,
            segments       = segments,
            grainLineAngle = 90f,
            seamAllowance  = sa,
            quantity       = 2,
            widthCm        = totalW,
            heightCm       = totalH
        )
    }

    // ════════════════════════════════════════════════════════
    // الخلفية — Back Panel
    //
    // الخلفية أعرض + حجرها أعمق × 3 + خصرها مرتفع
    //
    //     A'──────────────B'
    //    /                 \  ← خصر خلفي مائل
    //   /                   C'
    //   │                   │
    //   G'                  D'
    //   │                   │
    //   F'──────────────────E'
    //   ↑↑↑
    // حجر خلفي أعمق بكتير
    // ════════════════════════════════════════════════════════
    private fun backPanel(m: Measurements, model: TrouserModel): PatternPiece {
        val sa  = m.seamAllowance
        val cd  = FormulaEngine.crotchDepth(m)
        val hd  = FormulaEngine.hipDepth(m)
        val bw  = FormulaEngine.backHipWidth(m)
        val ww  = FormulaEngine.backWaistWidth(m)
        val ce  = FormulaEngine.backCrotchExtension(m)   // × 3 الأمامي
        val hw  = FormulaEngine.backHemWidth(m, model)
        val wl  = FormulaEngine.backWaistLift(m)         // ارتفاع الخصر الخلفي
        val len = m.length

        // ── Anchor Points ─────────────────────────────────
        // A: وسط الخصر الخلفي — مرتفع بـ wl
        val A = Point(sa + ce * 0.3f, sa)

        // B: جنب الخصر الخلفي — أعلى من A بـ wl
        val B = Point(sa + ce * 0.3f + ww, sa - wl)

        // C: الجنب عند الورك
        val C = Point(sa + ce * 0.3f + bw, sa + hd - wl * 0.5f)

        // D: الجنب أسفل
        val D = Point(sa + ce * 0.3f + bw, sa + cd + len)

        // E: الكمة داخلياً
        val E = Point(sa + ce * 0.3f + hw, sa + cd + len)

        // F: نقطة الحجر الخلفي — أعمق بكتير من الأمامي
        // بتنزل 2 سم تحت خط القعدة
        val F = Point(sa - ce * 0.7f, sa + cd + 2f)

        // ── نقطة تحكم الحجر الخلفي (أعمق) ───────────────
        // الخلفي له امتداد خارجي أكبر — معادلة Aldrich
        val crotchCtrl1 = Point(
            x = F.x - ce * 0.15f,   // يخرج للخارج أكتر
            y = F.y - cd * 0.25f
        )
        val crotchCtrl2 = Point(
            x = A.x - ce * 0.05f,
            y = A.y + cd * 0.35f
        )

        // ── بناء المسار ───────────────────────────────────
        val segments = mutableListOf<PathSegment>()

        // 1. خط الخصر الخلفي: A → B (مائل ومنحنى)
        segments.add(PathSegment.CurveTo(
            control1 = Point(A.x + ww * 0.35f, A.y - wl * 0.3f),
            control2 = Point(A.x + ww * 0.75f, B.y + wl * 0.2f),
            end      = B
        ))

        // 2. الجنب: B → C (منحنى الورك)
        segments.add(PathSegment.CurveTo(
            control1 = Point(B.x + 0.8f, B.y + hd * 0.35f),
            control2 = Point(C.x,        C.y - hd * 0.25f),
            end      = C
        ))

        // 3. الجنب للكمة: C → D (مستقيم)
        segments.add(PathSegment.LineTo(D))

        // 4. خط الكمة: D → E (مستقيم)
        segments.add(PathSegment.LineTo(E))

        // 5. الساق الداخلي: E → F (منحنى)
        segments.add(PathSegment.CurveTo(
            control1 = Point(E.x - (E.x - F.x) * 0.15f, E.y - len * 0.25f),
            control2 = Point(F.x + ce * 0.4f,            F.y + (E.y - F.y) * 0.25f),
            end      = F
        ))

        // 6. منحنى الحجر الخلفي: F → A
        // أعمق وأوسع من الأمامي
        segments.add(PathSegment.CurveTo(
            control1 = crotchCtrl1,
            control2 = crotchCtrl2,
            end      = A
        ))

        val totalW = bw + ce + sa * 2f + ce * 0.3f
        val totalH = cd + len + sa * 2f + wl

        return PatternPiece(
            id             = "back_panel",
            nameAr         = "الخلفية",
            startPoint     = A,
            segments       = segments,
            grainLineAngle = 90f,
            seamAllowance  = sa,
            quantity       = 2,
            widthCm        = totalW,
            heightCm       = totalH
        )
    }

    // ════════════════════════════════════════════════════════
    // الكمر — Waistband
    // ════════════════════════════════════════════════════════
    private fun waistband(m: Measurements, model: TrouserModel) =
        rectPiece(
            "waistband",
            "الكمر — ${model.waistbandType.nameAr}",
            FormulaEngine.waistbandLength(m),
            FormulaEngine.waistbandWidth(model.waistbandType),
            qty = 1
        )

    // كمة الساق
    private fun legBand(m: Measurements) =
        rectPiece(
            "leg_band", "كمة الساق",
            FormulaEngine.legBandLength(m),
            FormulaEngine.legBandWidth(),
            qty = 2
        )

    // ════════════════════════════════════════════════════════
    // الجيب الأمامي — شبه منحرف مع منحنى
    // ════════════════════════════════════════════════════════
    private fun frontPocketFacing(m: Measurements): PatternPiece {
        val w  = FormulaEngine.frontPocketWidth(m)
        val h  = FormulaEngine.frontPocketHeight()
        val p0 = Point(2f, 0f)
        return PatternPiece(
            id = "front_pocket_facing", nameAr = "واجهة الجيب الأمامي",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w - 2f, 0f)),
                PathSegment.CurveTo(
                    Point(w, h * 0.25f),
                    Point(w, h * 0.75f),
                    Point(w, h)
                ),
                PathSegment.LineTo(Point(0f, h)),
                PathSegment.LineTo(p0)
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    private fun frontPocketBag(m: Measurements) =
        rectPiece(
            "front_pocket_bag", "كيس الجيب الأمامي",
            FormulaEngine.frontPocketBagWidth(m),
            FormulaEngine.frontPocketBagHeight(),
            qty = 2
        )

    // ════════════════════════════════════════════════════════
    // الجيب الخلفي (بطاقة)
    // ════════════════════════════════════════════════════════
    private fun backWeltPocket(m: Measurements) =
        rectPiece(
            "back_welt", "بطاقة الجيب الخلفي",
            FormulaEngine.backWeltPocketWidth(m),
            FormulaEngine.backWeltPocketHeight(),
            qty = 2
        )

    private fun backWeltPocketBag(m: Measurements) =
        rectPiece(
            "back_pocket_bag", "كيس الجيب الخلفي",
            FormulaEngine.backWeltPocketBagWidth(m),
            FormulaEngine.backWeltPocketBagHeight(),
            qty = 2
        )

    // ════════════════════════════════════════════════════════
    // السفرة الخلفية — منحنى أسفل
    // ════════════════════════════════════════════════════════
    private fun backYoke(m: Measurements): PatternPiece {
        val w  = FormulaEngine.backYokeWidth(m)
        val h  = FormulaEngine.backYokeHeight()
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "back_yoke", nameAr = "السفرة الخلفية",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h)),
                PathSegment.CurveTo(
                    Point(w * 0.75f, h + 1.5f),
                    Point(w * 0.25f, h + 1.5f),
                    Point(0f, h)
                ),
                PathSegment.LineTo(p0)
            ),
            quantity = 1, widthCm = w, heightCm = h + 1.5f
        )
    }

    // ════════════════════════════════════════════════════════
    // البتلتة — منحنى أسفل
    // ════════════════════════════════════════════════════════
    private fun fly(m: Measurements): PatternPiece {
        val w  = FormulaEngine.flyWidth(m)
        val h  = FormulaEngine.flyHeight(m)
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "fly", nameAr = "البتلتة",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h * 0.75f)),
                PathSegment.CurveTo(
                    Point(w,        h),
                    Point(w * 0.5f, h),
                    Point(0f,       h)
                ),
                PathSegment.LineTo(p0)
            ),
            quantity = 1, widthCm = w, heightCm = h
        )
    }

    private fun flyFacing(m: Measurements) =
        rectPiece(
            "fly_facing", "واجهة البتلتة",
            FormulaEngine.flyFacingWidth(m),
            FormulaEngine.flyHeight(m),
            qty = 1
        )

    // ════════════════════════════════════════════════════════
    // جيب الكارجو
    // ════════════════════════════════════════════════════════
    private fun cargoPocket(m: Measurements) =
        rectPiece(
            "cargo_pocket", "جيب الكارجو",
            FormulaEngine.cargoPocketPleatWidth(m),
            FormulaEngine.cargoPocketHeight(),
            qty = 2
        )

    private fun cargoPocketFlap(m: Measurements): PatternPiece {
        val w  = FormulaEngine.cargoPocketFlapWidth(m)
        val h  = FormulaEngine.cargoPocketFlapHeight()
        val p0 = Point(0f, 0f)
        return PatternPiece(
            id = "cargo_flap", nameAr = "غطاء جيب الكارجو",
            startPoint = p0,
            segments = listOf(
                PathSegment.LineTo(Point(w, 0f)),
                PathSegment.LineTo(Point(w, h - 1f)),
                PathSegment.CurveTo(
                    Point(w,         h),
                    Point(w * 0.6f,  h),
                    Point(w * 0.5f,  h)
                ),
                PathSegment.CurveTo(
                    Point(w * 0.4f, h),
                    Point(0f,       h),
                    Point(0f,       h - 1f)
                ),
                PathSegment.LineTo(p0)
            ),
            quantity = 2, widthCm = w, heightCm = h
        )
    }

    // ════════════════════════════════════════════════════════
    // مساعد — مستطيل
    // ════════════════════════════════════════════════════════
    private fun rectPiece(
        id: String, nameAr: String,
        w: Float, h: Float, qty: Int
    ): PatternPiece {
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
