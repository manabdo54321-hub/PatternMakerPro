package com.patternmaker.domain.engine

import com.patternmaker.domain.model.BezierCurve
import com.patternmaker.domain.model.Point
import kotlin.math.sqrt

/**
 * CurveEngine — معادلات منحنيات احترافية مستوحاة من French Curve
 * المرجع: Winifred Aldrich "Metric Pattern Cutting"
 */
object CurveEngine {

    // ══════════════════════════════════════════════════════════
    // منحنى الكيلوت الأمامي (Front Crotch Curve)
    // ══════════════════════════════════════════════════════════
    fun frontCrotchCurve(
        crotchDepth: Float,   // عمق الكيلوت
        panelWidth:  Float    // عرض الأمامية
    ): BezierCurve {
        // نقطة البداية: أسفل الجنب
        val start = Point(0f, crotchDepth)

        // نقطة النهاية: أعلى الوسط الأمامي
        val end = Point(panelWidth * 0.25f, 0f)

        // نقاط التحكم المحسوبة بمعادلة Aldrich
        // control1: ثلث الارتفاع للداخل — يعطي انحناء طبيعي
        val control1 = Point(
            x = panelWidth * 0.05f,
            y = crotchDepth * 0.65f
        )
        // control2: ربع العرض للأعلى — يكمل الانحناء
        val control2 = Point(
            x = panelWidth * 0.20f,
            y = crotchDepth * 0.15f
        )

        return BezierCurve(start, control1, control2, end)
    }

    // ══════════════════════════════════════════════════════════
    // منحنى الكيلوت الخلفي (Back Crotch Curve)
    // أعمق وأوسع من الأمامي بمقدار ضعفين
    // ══════════════════════════════════════════════════════════
    fun backCrotchCurve(
        crotchDepth: Float,
        panelWidth:  Float
    ): BezierCurve {
        val start = Point(0f, crotchDepth)
        val end   = Point(panelWidth * 0.35f, 0f)

        // الخلفي له امتداد خارجي مميز (Back Crotch Extension)
        val extension = panelWidth * 0.08f

        val control1 = Point(
            x = -extension,               // يخرج للخارج قليلاً
            y = crotchDepth * 0.70f
        )
        val control2 = Point(
            x = panelWidth * 0.15f,
            y = crotchDepth * 0.10f
        )

        return BezierCurve(start, control1, control2, end)
    }

    // ══════════════════════════════════════════════════════════
    // منحنى الخصر الأمامي (Front Waist Curve)
    // انحناء خفيف للداخل
    // ══════════════════════════════════════════════════════════
    fun frontWaistCurve(
        panelWidth: Float,
        waistDip:   Float = panelWidth * 0.02f   // انخفاض الخصر الأمامي
    ): BezierCurve {
        val start = Point(0f, waistDip)
        val end   = Point(panelWidth, 0f)

        val control1 = Point(panelWidth * 0.25f, waistDip * 0.8f)
        val control2 = Point(panelWidth * 0.75f, waistDip * 0.2f)

        return BezierCurve(start, control1, control2, end)
    }

    // ══════════════════════════════════════════════════════════
    // منحنى الخصر الخلفي (Back Waist Curve)
    // أعمق من الأمامي — الظهر له ارتفاع طبيعي أعلى
    // ══════════════════════════════════════════════════════════
    fun backWaistCurve(
        panelWidth: Float,
        waistRise:  Float = panelWidth * 0.04f
    ): BezierCurve {
        val start = Point(0f, 0f)
        val end   = Point(panelWidth, waistRise)

        val control1 = Point(panelWidth * 0.30f, -waistRise * 0.5f)
        val control2 = Point(panelWidth * 0.70f, waistRise * 0.3f)

        return BezierCurve(start, control1, control2, end)
    }

    // ══════════════════════════════════════════════════════════
    // منحنى الساق الداخلي (Inner Leg Curve)
    // الانحناء الداخلي من الكيلوت للكمّة
    // ══════════════════════════════════════════════════════════
    fun innerLegCurve(
        legLength:    Float,
        crotchDepth:  Float,
        topLegWidth:  Float,
        bottomLegWidth: Float
    ): BezierCurve {
        val start = Point(topLegWidth, crotchDepth)
        val end   = Point(bottomLegWidth, legLength + crotchDepth)

        val control1 = Point(
            x = topLegWidth * 1.05f,     // انتفاخ خفيف في الأعلى
            y = crotchDepth + legLength * 0.25f
        )
        val control2 = Point(
            x = bottomLegWidth * 1.02f,
            y = crotchDepth + legLength * 0.75f
        )

        return BezierCurve(start, control1, control2, end)
    }

    // ══════════════════════════════════════════════════════════
    // دالة مساعدة: طول المنحنى التقريبي (Curve Length)
    // لحساب الهامش الصح
    // ══════════════════════════════════════════════════════════
    fun curveLength(curve: BezierCurve, steps: Int = 50): Float {
        var length = 0f
        var prev   = evaluateCubicBezier(curve, 0f)
        for (i in 1..steps) {
            val t    = i.toFloat() / steps
            val curr = evaluateCubicBezier(curve, t)
            val dx   = curr.x - prev.x
            val dy   = curr.y - prev.y
            length  += sqrt(dx * dx + dy * dy)
            prev     = curr
        }
        return length
    }

    // حساب نقطة على المنحنى عند t (0..1)
    fun evaluateCubicBezier(curve: BezierCurve, t: Float): Point {
        val mt  = 1f - t
        val mt2 = mt * mt
        val mt3 = mt2 * mt
        val t2  = t * t
        val t3  = t2 * t
        return Point(
            x = mt3 * curve.start.x +
                3f * mt2 * t * curve.control1.x +
                3f * mt * t2 * curve.control2.x +
                t3 * curve.end.x,
            y = mt3 * curve.start.y +
                3f * mt2 * t * curve.control1.y +
                3f * mt * t2 * curve.control2.y +
                t3 * curve.end.y
        )
    }

    // نقاط المنحنى كاملة للرسم
    fun curvePoints(curve: BezierCurve, steps: Int = 30): List<Point> =
        (0..steps).map { evaluateCubicBezier(curve, it.toFloat() / steps) }
}
