package com.patternmaker.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.patternmaker.domain.engine.NestingEngine
import com.patternmaker.domain.engine.NestingReport
import com.patternmaker.domain.engine.NestingResult
import com.patternmaker.domain.model.*

class NestingViewModel : ViewModel() {

    // ── إعدادات القماش ────────────────────────────────────────
    var fabricWidth       by mutableStateOf("150")
    var fabricLength      by mutableStateOf("")      // فارغ = أوتوماتيك
    var quantity          by mutableStateOf("1")
    var allowRotation     by mutableStateOf(true)
    var respectGrainLine  by mutableStateOf(true)
    var gap               by mutableStateOf("1.5")

    // ── النتيجة ────────────────────────────────────────────────
    var result            by mutableStateOf<NestingResult?>(null)
    var selectedPieceIndex by mutableStateOf(-1)

    val layout get() = result?.layout
    val report get() = result?.report

    fun autoNest(pieces: List<PatternPiece>) {
        val settings = FabricSettings(
            width          = fabricWidth.toFloatOrNull() ?: 150f,
            length         = fabricLength.toFloatOrNull(),
            quantity       = quantity.toIntOrNull()?.coerceIn(1, 20) ?: 1,
            gap            = gap.toFloatOrNull() ?: 1.5f,
            allowRotation  = allowRotation,
            respectGrainLine = respectGrainLine
        )
        result = NestingEngine.autoNest(pieces, settings)
        selectedPieceIndex = -1
    }

    fun movePiece(index: Int, dx: Float, dy: Float) {
        val current = layout ?: return
        val pieces  = current.placedPieces.toMutableList()
        val piece   = pieces[index]
        val w = if (piece.rotation == 90f) piece.piece.heightCm else piece.piece.widthCm
        pieces[index] = piece.copy(
            x = (piece.x + dx).coerceIn(0f, current.fabricWidth - w),
            y = (piece.y + dy).coerceAtLeast(0f)
        )
        result = result?.copy(layout = current.copy(placedPieces = pieces))
    }

    fun rotatePiece(index: Int) {
        val current = layout ?: return
        val pieces  = current.placedPieces.toMutableList()
        val piece   = pieces[index]
        pieces[index] = piece.copy(rotation = (piece.rotation + 90f) % 360f)
        result = result?.copy(layout = current.copy(placedPieces = pieces))
    }
}
