package com.patternmaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.ui.components.NestingCanvas
import com.patternmaker.viewmodel.NestingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestingScreen(
    pieces: List<PatternPiece>,
    onBack: () -> Unit
) {
    val vm: NestingViewModel = viewModel()
    val layout = vm.layout

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "التعشيق على القماش",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.autoNest(pieces) }) {
                        Icon(Icons.Default.Refresh, "تعشيق أوتوماتيك")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(12.dp)) {
                // إدخال عرض القماش
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("سم", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = vm.fabricWidth,
                        onValueChange = { vm.fabricWidth = it },
                        modifier = Modifier.width(90.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("عرض القماش:", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(8.dp))

                // خيار التدوير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("السماح بتدوير القطع")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = vm.allowRotation,
                        onCheckedChange = { vm.allowRotation = it }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // إحصائيات
                layout?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip("الهدر", "%.1f%%".format(it.wastePercent))
                        StatChip("الطول", "%.0f سم".format(it.fabricLength))
                        StatChip("القطع", "${it.placedPieces.size}")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // زر التعشيق
                Button(
                    onClick = { vm.autoNest(pieces) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("تعشيق أوتوماتيك")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (layout == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "اضغط تعشيق أوتوماتيك للبدء",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "أو اضبط عرض القماش أولاً",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                NestingCanvas(
                    layout          = layout,
                    selectedIndex   = vm.selectedPieceIndex,
                    onPieceSelected = { vm.selectedPieceIndex = it },
                    onPieceMoved    = { i, dx, dy -> vm.movePiece(i, dx, dy) }
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
