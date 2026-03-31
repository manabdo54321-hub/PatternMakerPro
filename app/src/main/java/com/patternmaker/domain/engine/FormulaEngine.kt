package com.patternmaker.domain.engine

import com.patternmaker.domain.model.*

object FormulaEngine {

    // ── الأمامية ──────────────────────────────────────────────
    fun frontWidth(m: Measurements, model: TrouserModel): Float =
        (m.hip + model.ease) / 4f

    fun frontCrotchDepth(m: Measurements, model: TrouserModel): Float =
        m.hip * model.frontCrotchRatio

    // ── الخلفية ───────────────────────────────────────────────
    fun backWidth(m: Measurements, model: TrouserModel): Float =
        (m.hip + model.ease) / 4f + 3f

    fun backCrotchDepth(m: Measurements, model: TrouserModel): Float =
        m.hip * model.backCrotchRatio

    // ── الكمر ─────────────────────────────────────────────────
    fun waistbandLength(m: Measurements): Float =
        m.waist + m.ease + (m.seamAllowance * 2f)

    fun waistbandWidth(type: WaistbandType): Float = type.widthCm

    // ── كمة الساق ─────────────────────────────────────────────
    fun legBandLength(m: Measurements): Float =
        m.legWidth + (m.seamAllowance * 2f)

    fun legBandWidth(): Float = 6f

    // ── تضييق الساق ───────────────────────────────────────────
    fun bottomLegWidth(m: Measurements, model: TrouserModel): Float =
        frontWidth(m, model) * (1f - model.legTaper)

    // ══════════════════════════════════════════════════════════
    // معادلات القطع الاختيارية
    // ══════════════════════════════════════════════════════════

    // جيب أمامي (Front Pocket)
    // شكل شبه منحرف — أعلى أضيق من أسفل
    fun frontPocketWidth(m: Measurements, model: TrouserModel): Float =
        frontWidth(m, model) * OptionalPiece.FRONT_POCKET.widthRatio

    fun frontPocketHeight(): Float = OptionalPiece.FRONT_POCKET.heightCm

    fun frontPocketBagWidth(m: Measurements, model: TrouserModel): Float =
        frontPocketWidth(m, model) + 3f   // الكيس أعرض من الفتحة

    fun frontPocketBagHeight(): Float = 22f

    // جيب خلفي بطاقة (Back Welt Pocket)
    fun backWeltPocketWidth(m: Measurements, model: TrouserModel): Float =
        backWidth(m, model) * OptionalPiece.BACK_WELT_POCKET.widthRatio

    fun backWeltPocketHeight(): Float = OptionalPiece.BACK_WELT_POCKET.heightCm

    fun backWeltPocketBagWidth(m: Measurements, model: TrouserModel): Float =
        backWeltPocketWidth(m, model) + 2f

    fun backWeltPocketBagHeight(): Float = 16f

    // سفرة خلفية (Back Yoke)
    // قطعة أفقية في أعلى الخلفية
    fun backYokeWidth(m: Measurements, model: TrouserModel): Float =
        backWidth(m, model) + (m.seamAllowance * 2f)

    fun backYokeHeight(): Float = OptionalPiece.BACK_YOKE.heightCm

    // بتلتة (Fly)
    // القطعة اللي بتغطي السوستة في الأمام
    fun flyWidth(m: Measurements, model: TrouserModel): Float =
        frontWidth(m, model) * OptionalPiece.FLY.widthRatio + m.seamAllowance

    fun flyHeight(m: Measurements, model: TrouserModel): Float =
        frontCrotchDepth(m, model) * 0.75f   // ثلاثة أرباع عمق الكيلوت

    fun flyFacingWidth(m: Measurements, model: TrouserModel): Float =
        flyWidth(m, model) - 1f

    // جيب كارجو (Cargo Pocket)
    // جيب كبير على الفخذ — بدّة + غطاء
    fun cargoPocketWidth(m: Measurements, model: TrouserModel): Float =
        frontWidth(m, model) * OptionalPiece.CARGO_POCKET.widthRatio

    fun cargoPocketHeight(): Float = OptionalPiece.CARGO_POCKET.heightCm

    fun cargoPocketFlapWidth(m: Measurements, model: TrouserModel): Float =
        cargoPocketWidth(m, model) + 2f   // الغطاء أعرض قليلاً

    fun cargoPocketFlapHeight(): Float = 6f

    fun cargoPocketPleatWidth(m: Measurements, model: TrouserModel): Float =
        cargoPocketWidth(m, model) + 6f   // البدّة تضيف 3 سم من كل جانب

    // ── إجمالي القماش المطلوب ─────────────────────────────────
    fun totalFabricLength(
        m: Measurements,
        model: TrouserModel,
        enabledPieces: List<OptionalPiece>
    ): Float {
        val legLength  = m.length + frontCrotchDepth(m, model) + (m.seamAllowance * 2f)
        val waistExtra = waistbandWidth(model.waistbandType) + m.seamAllowance
        val legExtra   = if (model.hasLegBand) legBandWidth() + m.seamAllowance else 0f
        val yokeExtra  = if (enabledPieces.contains(OptionalPiece.BACK_YOKE))
            backYokeHeight() + m.seamAllowance else 0f
        val cargoExtra = if (enabledPieces.contains(OptionalPiece.CARGO_POCKET))
            cargoPocketHeight() + cargoPocketFlapHeight() + m.seamAllowance else 0f
        return legLength + waistExtra + legExtra + yokeExtra + cargoExtra
    }
}
