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
import androidx.compose.ui.geometry.Size
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
    // SCALE: 1 سم = 4 بكسل
    val CM_TO_PX = 4f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(10f, 10f)) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.2f, 5f)
        offset += panChange
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDDDDDD))
            .transformable(state = transformState)
            .pointerInput(layout, scale, offset) {
                detectTapGestures { tapOffset ->
                    val cx = (tapOffset.x - offset.x) / scale / CM_TO_PX
                    val cy = (tapOffset.y - offset.y) / scale / CM_TO_PX
                    layout.placedPieces.forEachIndexed { i, placed ->
                        val w = if (placed.rotation == 90f) placed.piece.heightCm else placed.piece.widthCm
                        val h = if (placed.rotation == 90f) placed.piece.widthCm else placed.piece.heightCm
                        if (cx >= placed.x && cx <= placed.x + w &&
                            cy >= placed.y && cy <= placed.y + h) {
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
                            dragAmount.x / scale / CM_TO_PX,
                            dragAmount.y / scale / CM_TO_PX
                        )
                    }
                }
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {

            val fw = layout.fabricWidth * CM_TO_PX
            val fl = layout.fabricLength * CM_TO_PX

            // القماش
            drawRect(
                color = Color(0xFFFAF7F0),
                size  = Size(fw, fl)
            )

            // شبكة كل 10 سم
            var gx = 0f
            while (gx <= layout.fabricWidth) {
                drawLine(
                    Color(0xFFCCCCCC),
                    Offset(gx * CM_TO_PX, 0f),
                    Offset(gx * CM_TO_PX, fl),
                    0.5f
                )
                gx += 10f
            }
            var gy = 0f
            while (gy <= layout.fabricLength) {
                drawLine(
                    Color(0xFFCCCCCC),
                    Offset(0f, gy * CM_TO_PX),
                    Offset(fw, gy * CM_TO_PX),
                    0.5f
                )
                gy += 10f
            }

            // حدود القماش
            drawRect(
                color = Color(0xFF333333),
                size  = Size(fw, fl),
                style = Stroke(width = 2f)
            )

            // القطع
            layout.placedPieces.forEachIndexed { index, placed ->
                val color = PIECE_COLORS[index % PIECE_COLORS.size]
                val isSelected = index == selectedIndex
                val w = (if (placed.rotation == 90f) placed.piece.heightCm else placed.piece.widthCm) * CM_TO_PX
                val h = (if (placed.rotation == 90f) placed.piece.widthCm else placed.piece.heightCm) * CM_TO_PX
                val px = placed.x * CM_TO_PX
                val py = placed.y * CM_TO_PX

                drawRect(
                    color   = color.copy(alpha = if (isSelected) 0.5f else 0.3f),
                    topLeft = Offset(px, py),
                    size    = Size(w, h)
                )
                drawRect(
                    color   = if (isSelected) Color.Red else color,
                    topLeft = Offset(px, py),
                    size    = Size(w, h),
                    style   = Stroke(width = if (isSelected) 3f else 1.5f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize       = 12f
                        this.color     = android.graphics.Color.BLACK
                        textAlign      = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                    }
                    drawText(placed.piece.nameAr, px + w / 2f, py + h / 2f, paint)
                    val paint2 = android.graphics.Paint().apply {
                        textSize   = 10f
                        this.color = android.graphics.Color.DKGRAY
                        textAlign  = android.graphics.Paint.Align.CENTER
                    }
                    drawText(
                        "%.0f×%.0f".format(
                            placed.piece.widthCm,
                            placed.piece.heightCm
                        ),
                        px + w / 2f,
                        py + h / 2f + 14f,
                        paint2
                    )
                }
            }
        }
    }
}
