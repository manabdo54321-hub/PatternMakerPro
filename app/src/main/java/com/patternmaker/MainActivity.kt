package com.patternmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patternmaker.ui.screens.MeasurementsScreen
import com.patternmaker.ui.screens.PatternScreen
import com.patternmaker.ui.theme.PatternMakerTheme
import com.patternmaker.viewmodel.MeasurementsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatternMakerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: MeasurementsViewModel = viewModel()
                    var screen by remember { mutableStateOf("measurements") }

                    when (screen) {
                        "measurements" -> MeasurementsScreen(
                            viewModel = vm,
                            onPatternGenerated = { screen = "pattern" }
                        )
                        "pattern" -> PatternScreen(
                            pieces = vm.generatedPieces,
                            onBack = { screen = "measurements" },
                            onGoToNesting = { screen = "nesting" }
                        )
                    }
                }
            }
        }
    }
}
