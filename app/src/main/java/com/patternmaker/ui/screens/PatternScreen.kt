package com.patternmaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.ui.components.PatternCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternScreen(
    pieces: List<PatternPiece>,
    onBack: () -> Unit,
    onGoToNesting: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "عرض الباترون",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
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
            Column {
                // قائمة القطع
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    items(pieces) { piece ->
                        SuggestionChip(
                            onClick = {},
                            label  = { Text("${piece.nameAr} ×${piece.quantity}") },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
                // زر التعشيق
                Button(
                    onClick  = onGoToNesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                ) {
                    Text("انتقل للتعشيق على القماش ←")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (pieces.isEmpty()) {
                Text(
                    text = "لا توجد قطع — ارجع وأدخل المقاسات",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                PatternCanvas(pieces = pieces)
            }
        }
    }
}
