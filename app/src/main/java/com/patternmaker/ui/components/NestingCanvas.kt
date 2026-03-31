package com.patternmaker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.patternmaker.domain.model.NestingLayout
import com.patternmaker.domain.model.PlacedPiece

private val PIECE_COLORS = listOf(
    Color(0xFF1B4F72),
    Color(0xFF148F77),
    Color(0xFF7D3C98),
    Color(0xFFB7950B),
    Color(0xFFCB4335),
)

@Composable
fun NestingCanvas(
    layout: NestingLayout,
    selectedIndex: Int,
    onPieceSelected: (Int) -> Unit,
    onPieceMoved: (Int, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(2f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.3f, 8f)
        offset += panChange
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8E8E8))
            .transformable(state = transformState)
            .pointerInput(layout) {
                detectTapGestures { tapOffset ->
                    val canvasX = (tapOffset.x - offset.x) / scale
                    val canvasY = (tapOffset.y - offset.y) / scale
                    layout.placedPieces.forEachIndexed { i, placed ->
                        if (canvasX >= placed.x && canvasX <= placed.x + placed.piece.widthCm &&
                            canvasY >= placed.y && canvasY <= placed.y + placed.piece.heightCm) {
                            onPieceSelected(i)
                        }
                    }
                }
            }
            .pointerInput(selectedIndex) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (selectedIndex >= 0) {
                        onPieceMoved(
                            selectedIndex,
                            dragAmount.x / scale,
                            dragAmount.y / scale
                        )
                    }
                }
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {
            // رسم القماش
            drawRect(
                color   = Color(0xFFFAF7F0),
                size    = androidx.compose.ui.geometry.Size(
                    layout.fabricWidth,
                    layout.fabricLength
                )
            )
            // حدود القماش
            drawRect(
                color = Color(0xFF333333),
                size  = androidx.compose.ui.geometry.Size(
                    layout.fabricWidth,
                    layout.fabricLength
                ),
                style = Stroke(width = 1f)
            )

            // خطوط كل 10 سم
            var x = 10f
            while (x < layout.fabricWidth) {
                drawLine(Color(0xFFDDDDDD), Offset(x, 0f), Offset(x, layout.fabricLength), 0.3f)
                x += 10f
            }
            var y = 10f
            while (y < layout.fabricLength) {
                drawLine(Color(0xFFDDDDDD), Offset(0f, y), Offset(layout.fabricWidth, y), 0.3f)
                y += 10f
            }

            // رسم القطع
            layout.placedPieces.forEachIndexed { index, placed ->
                val color = PIECE_COLORS[index % PIECE_COLORS.size]
                val isSelected = index == selectedIndex
                drawPlacedPiece(placed, color, isSelected)
            }
        }
    }
}

private fun DrawScope.drawPlacedPiece(
    placed: PlacedPiece,
    color: Color,
    isSelected: Boolean
) {
    val w = if (placed.rotation == 90f || placed.rotation == 270f)
        placed.piece.heightCm else placed.piece.widthCm
    val h = if (placed.rotation == 90f || placed.rotation == 270f)
        placed.piece.widthCm else placed.piece.heightCm

    val rect = androidx.compose.ui.geometry.Size(w, h)

    // تلوين القطعة
    drawRect(
        color    = color.copy(alpha = if (isSelected) 0.5f else 0.3f),
        topLeft  = Offset(placed.x, placed.y),
        size     = rect
    )

    // حدود القطعة
    drawRect(
        color   = if (isSelected) Color.Red else color,
        topLeft = Offset(placed.x, placed.y),
        size    = rect,
        style   = Stroke(width = if (isSelected) 1.5f else 0.8f)
    )

    // اسم القطعة
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize       = 4f
            this.color     = android.graphics.Color.BLACK
            textAlign      = android.graphics.Paint.Align.CENTER
            isFakeBoldText = isSelected
        }
        drawText(
            placed.piece.nameAr,
            placed.x + w / 2f,
            placed.y + h / 2f,
            paint
        )
        // القياسات
        val paint2 = android.graphics.Paint().apply {
            textSize   = 3f
            this.color = android.graphics.Color.DKGRAY
            textAlign  = android.graphics.Paint.Align.CENTER
        }
        drawText(
            "%.0f×%.0f".format(w, h),
            placed.x + w / 2f,
            placed.y + h / 2f + 5f,
            paint2
        )
    }
}
