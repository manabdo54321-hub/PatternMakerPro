package com.patternmaker.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.patternmaker.domain.engine.NestingEngine
import com.patternmaker.domain.engine.WasteReport
import com.patternmaker.domain.model.NestingLayout
import com.patternmaker.domain.model.PatternPiece

class NestingViewModel : ViewModel() {

    var fabricWidth    by mutableStateOf("150")
    var allowRotation  by mutableStateOf(true)
    var layout         by mutableStateOf<NestingLayout?>(null)
    var wasteReport    by mutableStateOf<WasteReport?>(null)
    var selectedPieceIndex by mutableStateOf(-1)

    fun autoNest(pieces: List<PatternPiece>) {
        val width = fabricWidth.toFloatOrNull() ?: 150f
        val result = NestingEngine.autoNest(pieces, width, allowRotation)
        layout      = result
        wasteReport = NestingEngine.calculateWaste(result)
        selectedPieceIndex = -1
    }

    fun movePiece(index: Int, dx: Float, dy: Float) {
        val current = layout ?: return
        val pieces  = current.placedPieces.toMutableList()
        val piece   = pieces[index]
        pieces[index] = piece.copy(
            x = (piece.x + dx).coerceAtLeast(0f)
                .coerceAtMost(current.fabricWidth - piece.piece.widthCm),
            y = (piece.y + dy).coerceAtLeast(0f)
        )
        layout      = current.copy(placedPieces = pieces)
        wasteReport = NestingEngine.calculateWaste(layout!!)
    }

    fun rotatePiece(index: Int) {
        val current = layout ?: return
        val pieces  = current.placedPieces.toMutableList()
        val piece   = pieces[index]
        pieces[index] = piece.copy(rotation = (piece.rotation + 90f) % 360f)
        layout = current.copy(placedPieces = pieces)
    }
}
