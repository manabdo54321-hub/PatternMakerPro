package com.patternmaker.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.patternmaker.domain.engine.PatternEngine
import com.patternmaker.domain.model.Measurements
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.TrouserModel

class MeasurementsViewModel : ViewModel() {

    var clientName by mutableStateOf("")
    var waist      by mutableStateOf("90")
    var hip        by mutableStateOf("100")
    var length     by mutableStateOf("100")
    var legWidth   by mutableStateOf("22")
    var rise       by mutableStateOf("30")
    var seamAllowance by mutableStateOf("1.5")
    var ease       by mutableStateOf("6")
    var selectedModel by mutableStateOf(TrouserModel.SWEATPANTS)

    var generatedPieces by mutableStateOf<List<PatternPiece>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun generatePattern() {
        errorMessage = null
        try {
            val m = Measurements(
                clientName    = clientName,
                waist         = waist.toFloat(),
                hip           = hip.toFloat(),
                length        = length.toFloat(),
                legWidth      = legWidth.toFloat(),
                rise          = rise.toFloat(),
                seamAllowance = seamAllowance.toFloat(),
                ease          = ease.toFloat()
            )
            if (m.waist <= 0 || m.hip <= 0 || m.length <= 0) {
                errorMessage = "تأكد من إدخال جميع المقاسات بشكل صحيح"
                return
            }
            generatedPieces = PatternEngine.generateTrouser(m, selectedModel)
        } catch (e: NumberFormatException) {
            errorMessage = "أرقام غير صحيحة — تأكد من المقاسات"
        }
    }

    fun applyStandardSize(size: StandardSize) {
        waist     = size.waist.toString()
        hip       = size.hip.toString()
        length    = size.length.toString()
        legWidth  = size.legWidth.toString()
        rise      = size.rise.toString()
    }
}

enum class StandardSize(
    val label: String,
    val waist: Int,
    val hip: Int,
    val length: Int,
    val legWidth: Int,
    val rise: Int
) {
    S  ("S",   76,  86, 98, 20, 28),
    M  ("M",   82,  92, 100, 21, 29),
    L  ("L",   88,  98, 102, 22, 30),
    XL ("XL",  94, 104, 104, 23, 31),
    XXL("XXL", 100, 110, 106, 24, 32)
}
