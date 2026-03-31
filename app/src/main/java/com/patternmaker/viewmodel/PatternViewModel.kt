package com.patternmaker.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.patternmaker.domain.model.PatternPiece

class PatternViewModel : ViewModel() {
    var pieces by mutableStateOf<List<PatternPiece>>(emptyList())
    var showSeamAllowance by mutableStateOf(true)
    var showGrainLine by mutableStateOf(true)
}
