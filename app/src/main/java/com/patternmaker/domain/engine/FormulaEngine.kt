package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

// ═══════════════════════════════════════════════════════════════
// FormulaEngine — معادلات احترافية مستوحاة من:
// Winifred Aldrich + Helen Joseph-Armstrong + M.Müller & Sohn
// كل معادلة مبنية على المقاسات الحقيقية للجسم
// ═══════════════════════════════════════════════════════════════
object FormulaEngine {

    // ════════════════════════════════════════════════════════
    // عمق الحجر (Crotch Depth / Body Rise)
    // المسافة من الخصر لخط القعدة
    // ════════════════════════════════════════════════════════
    fun crotchDepth(m: Measurements): Float =
        m.rise + 1.5f   // القياس الحقيقي + 1.5 سم راحة

    // ════════════════════════════════════════════════════════
    // عمق الورك (Hip Depth)
    // المسافة من الخصر لأعرض نقطة في الأرداف
    // ═══════════════════════════════════════════════════════
    fun hipDepth(m: Measurements): Float =
        m.hipDepth.takeIf { it > 0f } ?: (m.rise * 0.65f)

    // ════════════════════════════════════════════════════════
    // الأمامية — Front Panel
    // ════════════════════════════════════════════════════════

    // عرض الخصر الأمامي: ربع الخصر - 1 سم
    fun frontWaistWidth(m: Measurements): Float =
        (m.waist + m.ease * 0.5f) / 4f - 1f

    // عرض الأرداف الأمامي: ربع الأرداف - 1 سم
    fun frontHipWidth(m: Measurements): Float =
        (m.hip + m.ease) / 4f - 1f

    // امتداد الحجر الأمامي: ربع عرض الأرداف الأمامي
    // المعادلة الاحترافية: hip/4 ÷ 4 = hip/16
    fun frontCrotchExtension(m: Measurements): Float =
        frontHipWidth(m) / 4f

    // عرض الكمة الأمامية
    fun frontHemWidth(m: Measurements, model: TrouserModel): Float =
        (m.legWidth / 2f) - 1f

    // ════════════════════════════════════════════════════════
    // الخلفية — Back Panel
    // ════════════════════════════════════════════════════════

    // عرض الخصر الخلفي: ربع الخصر + 1 سم
    // (الخلفية أعرض من الأمامية دائماً)
    fun backWaistWidth(m: Measurements): Float =
        (m.waist + m.ease * 0.5f) / 4f + 1f

    // عرض الأرداف الخلفي: ربع الأرداف + 1 سم
    fun backHipWidth(m: Measurements): Float =
        (m.hip + m.ease) / 4f + 1f

    // امتداد الحجر الخلفي = امتداد الأمامي × 3
    // ده القانون الاحترافي الثابت في كل مراجع الباترون
    fun backCrotchExtension(m: Measurements): Float =
        frontCrotchExtension(m) * 3f

    // ارتفاع الخلفية عند الخصر (Back Rise Lift)
    // الظهر بيرتفع عشان يغطي الأرداف عند الجلوس
    fun backWaistLift(m: Measurements): Float =
        backHipWidth(m) * 0.04f + 0.5f

    // عرض الكمة الخلفية
    fun backHemWidth(m: Measurements, model: TrouserModel): Float =
        (m.legWidth / 2f) + 1f

    // ════════════════════════════════════════════════════════
    // البنسة الخلفية (Back Dart)
    // الفرق بين الأرداف والخصر يتحول لبنسة
    // ════════════════════════════════════════════════════════
    fun backDartWidth(m: Measurements): Float {
        val diff = backHipWidth(m) - backWaistWidth(m)
        return diff.coerceIn(1.5f, 3.0f)   // البنسة بين 1.5 و 3 سم
    }

    fun backDartLength(m: Measurements): Float =
        hipDepth(m) * 0.75f   // ثلاثة أرباع عمق الورك

    // ════════════════════════════════════════════════════════
    // الكمر — Waistband
    // ════════════════════════════════════════════════════════
    fun waistbandLength(m: Measurements): Float =
        m.waist + m.ease + (m.seamAllowance * 2f) + 3f   // 3 سم تداخل

    fun waistbandWidth(type: WaistbandType): Float = type.widthCm

    // ════════════════════════════════════════════════════════
    // كمة الساق — Leg Band
    // ════════════════════════════════════════════════════════
    fun legBandLength(m: Measurements): Float =
        m.legWidth + (m.seamAllowance * 2f)

    fun legBandWidth(): Float = 6f

    // ════════════════════════════════════════════════════════
    // القطع الاختيارية
    // ════════════════════════════════════════════════════════

    // جيب أمامي
    fun frontPocketWidth(m: Measurements): Float =
        frontHipWidth(m) * 0.45f

    fun frontPocketHeight(): Float = 16f

    fun frontPocketBagWidth(m: Measurements): Float =
        frontPocketWidth(m) + 3f

    fun frontPocketBagHeight(): Float = 22f

    // جيب خلفي بطاقة
    fun backWeltPocketWidth(m: Measurements): Float =
        backHipWidth(m) * 0.35f

    fun backWeltPocketHeight(): Float = 2.5f

    fun backWeltPocketBagWidth(m: Measurements): Float =
        backWeltPocketWidth(m) + 2f

    fun backWeltPocketBagHeight(): Float = 16f

    // سفرة خلفية
    fun backYokeWidth(m: Measurements): Float =
        backHipWidth(m) + (m.seamAllowance * 2f)

    fun backYokeHeight(): Float = 8f

    // بتلتة
    fun flyWidth(m: Measurements): Float =
        frontCrotchExtension(m) * 1.2f + m.seamAllowance

    fun flyHeight(m: Measurements): Float =
        crotchDepth(m) * 0.70f

    fun flyFacingWidth(m: Measurements): Float =
        flyWidth(m) - 1f

    // جيب كارجو
    fun cargoPocketWidth(m: Measurements): Float =
        frontHipWidth(m) * 0.40f

    fun cargoPocketHeight(): Float = 20f

    fun cargoPocketFlapWidth(m: Measurements): Float =
        cargoPocketWidth(m) + 2f

    fun cargoPocketFlapHeight(): Float = 6f

    fun cargoPocketPleatWidth(m: Measurements): Float =
        cargoPocketWidth(m) + 6f

    // ════════════════════════════════════════════════════════
    // إجمالي القماش المطلوب
    // ════════════════════════════════════════════════════════
    fun totalFabricLength(
        m: Measurements,
        model: TrouserModel,
        enabledPieces: List<OptionalPiece>
    ): Float {
        val legLength  = m.length + crotchDepth(m) + (m.seamAllowance * 2f)
        val waistExtra = waistbandWidth(model.waistbandType) + m.seamAllowance
        val legExtra   = if (model.hasLegBand) legBandWidth() + m.seamAllowance else 0f
        val yokeExtra  = if (enabledPieces.contains(OptionalPiece.BACK_YOKE))
            backYokeHeight() + m.seamAllowance else 0f
        val cargoExtra = if (enabledPieces.contains(OptionalPiece.CARGO_POCKET))
            cargoPocketHeight() + cargoPocketFlapHeight() + m.seamAllowance else 0f
        return legLength + waistExtra + legExtra + yokeExtra + cargoExtra
    }
}
