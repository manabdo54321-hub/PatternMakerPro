package com.patternmaker.domain.engine

import com.patternmaker.domain.model.Measurements
import com.patternmaker.domain.model.TrouserModel

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

    // ── الخصر ─────────────────────────────────────────────────
    fun waistbandLength(m: Measurements): Float =
        m.waist + m.ease + (m.seamAllowance * 2f)

    fun waistbandWidth(): Float = 8f

    // ── كمة الساق ─────────────────────────────────────────────
    fun legBandLength(m: Measurements): Float =
        m.legWidth + (m.seamAllowance * 2f)

    fun legBandWidth(): Float = 6f

    // ── عرض الساق السفلي بعد التضييق ─────────────────────────
    fun bottomLegWidth(m: Measurements, model: TrouserModel): Float =
        frontWidth(m, model) * (1f - model.legTaper)

    // ── إجمالي الطول المطلوب من القماش ───────────────────────
    fun totalFabricLength(m: Measurements, model: TrouserModel): Float {
        val legLength = m.length + frontCrotchDepth(m, model) + (m.seamAllowance * 2f)
        val waistExtra = if (model.hasWaistBand) waistbandWidth() + m.seamAllowance else 0f
        val legExtra   = if (model.hasLegBand)  legBandWidth()  + m.seamAllowance else 0f
        return legLength + waistExtra + legExtra
    }
}
