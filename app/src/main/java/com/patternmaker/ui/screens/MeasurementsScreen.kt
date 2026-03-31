package com.patternmaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patternmaker.domain.model.TrouserModel
import com.patternmaker.ui.components.MeasurementField
import com.patternmaker.viewmodel.MeasurementsViewModel
import com.patternmaker.viewmodel.StandardSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementsScreen(
    viewModel: MeasurementsViewModel,
    onPatternGenerated: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إدخال المقاسات",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.height(12.dp))

            // ── اسم العميل ────────────────────────────────────
            OutlinedTextField(
                value = viewModel.clientName,
                onValueChange = { viewModel.clientName = it },
                label = { Text("اسم العميل (اختياري)", textAlign = TextAlign.Right) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ── اختيار الموديل ────────────────────────────────
            Text(
                text = "نوع الشروال",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TrouserModel.ALL.forEach { model ->
                    FilterChip(
                        selected = viewModel.selectedModel.id == model.id,
                        onClick  = { viewModel.selectedModel = model },
                        label    = { Text(model.nameAr) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── مقاسات قياسية ─────────────────────────────────
            Text(
                text = "مقاسات قياسية جاهزة",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                StandardSize.entries.forEach { size ->
                    OutlinedButton(
                        onClick  = { viewModel.applyStandardSize(size) },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    ) {
                        Text(size.label)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // ── حقول المقاسات ─────────────────────────────────
            Text(
                text = "المقاسات التفصيلية",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(8.dp))

            MeasurementField("محيط الخصر",  viewModel.waist,    { viewModel.waist    = it })
            MeasurementField("محيط الورك",  viewModel.hip,      { viewModel.hip      = it })
            MeasurementField("طول الشروال", viewModel.length,   { viewModel.length   = it })
            MeasurementField("عرض الساق",   viewModel.legWidth, { viewModel.legWidth = it })
            MeasurementField("الارتفاع",    viewModel.rise,     { viewModel.rise     = it })

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            MeasurementField("هامش الخياطة", viewModel.seamAllowance, { viewModel.seamAllowance = it })
            MeasurementField("توسعة الراحة", viewModel.ease,          { viewModel.ease          = it })

            // ── رسالة خطأ ─────────────────────────────────────
            viewModel.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── زر التوليد ────────────────────────────────────
            Button(
                onClick = {
                    viewModel.generatePattern()
                    if (viewModel.generatedPieces.isNotEmpty()) {
                        onPatternGenerated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("توليد الباترون", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
