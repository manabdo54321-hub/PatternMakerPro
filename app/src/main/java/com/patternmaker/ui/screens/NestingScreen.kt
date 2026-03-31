package com.patternmaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val report = vm.report
    val sel    = vm.selectedPieceIndex
    var showReport by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("التعشيق على القماش",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    // زر التقرير
                    if (report != null) {
                        TextButton(onClick = { showReport = true }) {
                            Text("التقرير", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    IconButton(onClick = { vm.autoNest(pieces) }) {
                        Icon(Icons.Default.Refresh, "تعشيق")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {

                // إعدادات القماش
                if (showSettings) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            // عرض + طول
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FabricInput("الطول (فارغ=أوتو)",
                                    vm.fabricLength, { vm.fabricLength = it })
                                Spacer(Modifier.width(8.dp))
                                FabricInput("العرض سم",
                                    vm.fabricWidth, { vm.fabricWidth = it })
                            }

                            Spacer(Modifier.height(8.dp))

                            // عدد الأطقم + المسافة
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FabricInput("مسافة سم",
                                    vm.gap, { vm.gap = it }, width = 80)
                                Spacer(Modifier.width(8.dp))
                                FabricInput("عدد الأطقم",
                                    vm.quantity, { vm.quantity = it },
                                    isDecimal = false, width = 80)
                                Spacer(Modifier.width(12.dp))
                                Text("الأطقم / المسافة",
                                    style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(Modifier.height(8.dp))

                            // خيارات
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LabeledSwitch("تدوير",
                                    vm.allowRotation, { vm.allowRotation = it })
                                LabeledSwitch("اتجاه الخامة",
                                    vm.respectGrainLine, { vm.respectGrainLine = it })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // إحصائيات سريعة
                report?.let { r ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniStat("الهدر",
                            "%.1f%%".format(r.wastePercent),
                            if (r.wastePercent < 15f) Color(0xFF148F77)
                            else if (r.wastePercent < 25f) Color(0xFFB7950B)
                            else Color(0xFFCB4335))
                        MiniStat("الكفاءة", "%.1f%%".format(r.efficiency))
                        MiniStat("الطول",   "%.0f سم".format(r.fabricLength))
                        MiniStat("الأمتار", "%.2f م".format(r.fabricMeters))
                        MiniStat("القطع",
                            "${r.placedPieces}/${r.totalPieces}",
                            if (r.missingPieces > 0) Color(0xFFCB4335)
                            else Color(0xFF148F77))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // أزرار التحكم
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sel >= 0) {
                        OutlinedButton(
                            onClick  = { vm.rotatePiece(sel) },
                            modifier = Modifier.weight(1f)
                        ) { Text("↻ تدوير") }
                        OutlinedButton(
                            onClick  = { vm.selectedPieceIndex = -1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("✕ إلغاء") }
                    }
                    Button(
                        onClick  = {
                            vm.autoNest(pieces)
                            showSettings = false
                        },
                        modifier = Modifier.weight(2f).height(48.dp)
                    ) { Text("✦ تعشيق أوتوماتيك") }
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
                    Text("اضبط الإعدادات ثم اضغط تعشيق",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("الإعداد الافتراضي: عرض 150 سم، طق واحد",
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

    // ── نافذة التقرير التفصيلي ────────────────────────────────
    if (showReport && report != null) {
        AlertDialog(
            onDismissRequest = { showReport = false },
            title = {
                Text("تقرير التعشيق التفصيلي",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    ReportRow("عرض القماش",  "%.0f سم".format(report.fabricWidth))
                    ReportRow("طول القماش",  "%.0f سم".format(report.fabricLength))
                    ReportRow("أمتار مطلوبة","%.2f متر".format(report.fabricMeters))
                    ReportRow("عدد الأطقم",  "${report.quantity}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ReportRow("مساحة القماش","%.0f سم²".format(report.fabricArea))
                    ReportRow("مساحة مستخدمة","%.0f سم²".format(report.usedArea))
                    ReportRow("مساحة الهدر", "%.0f سم²".format(report.wasteArea))
                    ReportRow("نسبة الهدر",
                        "%.1f%%".format(report.wastePercent),
                        if (report.wastePercent < 15f) Color(0xFF148F77)
                        else Color(0xFFCB4335))
                    ReportRow("كفاءة التعشيق","%.1f%%".format(report.efficiency),
                        Color(0xFF148F77))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("تفصيل القطع:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right)
                    Spacer(Modifier.height(4.dp))
                    report.pieceBreakdown.forEach { pc ->
                        ReportRow(pc.nameAr,
                            "${pc.placed} / ${pc.requested}",
                            if (pc.placed < pc.requested) Color(0xFFCB4335)
                            else Color(0xFF148F77))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReport = false }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
private fun FabricInput(
    label: String, value: String, onValueChange: (String) -> Unit,
    isDecimal: Boolean = true, width: Int = 100
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier.width(width.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
        singleLine = true
    )
}

@Composable
private fun LabeledSwitch(
    label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MiniStat(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
