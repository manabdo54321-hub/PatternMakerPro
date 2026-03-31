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
import androidx.compose.ui.graphics.Color
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
    val vm     = viewModel<NestingViewModel>()
    val layout = vm.layout
    val report = vm.wasteReport
    val sel    = vm.selectedPieceIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("التعشيق على القماش",
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.autoNest(pieces) }) {
                        Icon(Icons.Default.Refresh, "تعشيق")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(12.dp)) {

                // عرض القماش
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("سم")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = vm.fabricWidth,
                        onValueChange = { vm.fabricWidth = it },
                        modifier = Modifier.width(90.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("عرض القماش:", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(6.dp))

                // خيار التدوير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("السماح بتدوير القطع")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = vm.allowRotation,
                        onCheckedChange = { vm.allowRotation = it })
                }

                // زر تدوير القطعة المحددة
                if (sel >= 0) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { vm.rotatePiece(sel) }) {
                            Text("↻  تدوير القطعة المحددة 90°")
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { vm.selectedPieceIndex = -1 },
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("✕ إلغاء التحديد")
                        }
                    }
                }

                // إحصائيات
                report?.let { r ->
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("الهدر",  "%.1f%%".format(r.wastePercent),
                            if (r.wastePercent < 20f)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer)
                        StatCard("الطول", "%.0f سم".format(r.fabricLength))
                        StatCard("القطع", "${layout?.placedPieces?.size}")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick  = { vm.autoNest(pieces) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("✦  تعشيق أوتوماتيك")
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
                    Text("اضبط عرض القماش ثم اضغط تعشيق",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("القماش المصري عادةً 150 سم",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall)
                }
            } else {
                NestingCanvas(
                    layout          = layout,
                    selectedIndex   = sel,
                    onPieceSelected = { vm.selectedPieceIndex = it },
                    onPieceMoved    = { i, dx, dy -> vm.movePiece(i, dx, dy) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
