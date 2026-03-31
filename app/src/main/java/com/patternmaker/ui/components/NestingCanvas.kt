package com.patternmaker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.patternmaker.domain.model.NestingLayout

private val PIECE_COLORS = listOf(
    Color(0xFF1B4F72), Color(0xFF148F77),
    Color(0xFF7D3C98), Color(0xFFB7950B),
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
    val CM = 4f
    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(10f, 10f)) }

    // Zoom + Pan بإصبعين
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale  = (scale * zoomChange).coerceIn(0.2f, 8f)
        offset += panChange
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFCCCCCC))
            .transformable(state = transformState)

            // اختيار قطعة بالضغط
            .pointerInput(layout, scale, offset) {
                detectTapGestures { tap ->
                    val cx = (tap.x - offset.x) / scale / CM
                    val cy = (tap.y - offset.y) / scale / CM
                    var found = -1
                    layout.placedPieces.forEachIndexed { i, placed ->
                        val w = if (placed.rotation == 90f) placed.piece.heightCm else placed.piece.widthCm
                        val h = if (placed.rotation == 90f) placed.piece.widthCm  else placed.piece.heightCm
                        if (cx >= placed.x && cx <= placed.x + w &&
                            cy >= placed.y && cy <= placed.y + h) {
                            found = i
                        }
                    }
                    onPieceSelected(found)
                }
            }

            // سحب القطعة المحددة بإصبع واحد
            .pointerInput(selectedIndex, scale) {
                detectDragGestures(
                    onDragStart = {},
                    onDrag = { change, drag ->
                        change.consume()
                        if (selectedIndex >= 0) {
                            onPieceMoved(
                                selectedIndex,
                                drag.x / scale / CM,
                                drag.y / scale / CM
                            )
                        }
                    }
                )
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {
            val fw = layout.fabricWidth  * CM
            val fl = layout.fabricLength * CM

            // ── خلفية القماش ──────────────────────────────
            drawRect(Color(0xFFFAF7F0), size = Size(fw, fl))

            // ── شبكة 10 سم ────────────────────────────────
            var gx = 0f
            while (gx <= layout.fabricWidth) {
                val thick = gx % 50 == 0f
                drawLine(if (thick) Color(0xFFAAAAAA) else Color(0xFFDDDDDD),
                    Offset(gx * CM, 0f), Offset(gx * CM, fl),
                    strokeWidth = if (thick) 0.8f else 0.3f)
                gx += 10f
            }
            var gy = 0f
            while (gy <= layout.fabricLength) {
                val thick = gy % 50 == 0f
                drawLine(if (thick) Color(0xFFAAAAAA) else Color(0xFFDDDDDD),
                    Offset(0f, gy * CM), Offset(fw, gy * CM),
                    strokeWidth = if (thick) 0.8f else 0.3f)
                gy += 10f
            }

            // ── حدود القماش ───────────────────────────────
            drawRect(Color(0xFF222222), size = Size(fw, fl),
                style = Stroke(width = 2f))

            // ── القطع ─────────────────────────────────────
            layout.placedPieces.forEachIndexed { i, placed ->
                val color      = PIECE_COLORS[i % PIECE_COLORS.size]
                val isSelected = i == selectedIndex
                val w = (if (placed.rotation == 90f) placed.piece.heightCm else placed.piece.widthCm) * CM
                val h = (if (placed.rotation == 90f) placed.piece.widthCm  else placed.piece.heightCm) * CM
                val px = placed.x * CM
                val py = placed.y * CM

                // ظل للقطعة المحددة
                if (isSelected) {
                    drawRect(Color(0x33000000),
                        topLeft = Offset(px + 3f, py + 3f),
                        size    = Size(w, h))
                }

                // تلوين
                drawRect(color.copy(alpha = if (isSelected) 0.55f else 0.30f),
                    topLeft = Offset(px, py), size = Size(w, h))

                // حدود
                drawRect(if (isSelected) Color(0xFFE74C3C) else color,
                    topLeft = Offset(px, py), size = Size(w, h),
                    style = Stroke(width = if (isSelected) 3f else 1.5f))

                // مقبض التدوير لو محدد
                if (isSelected) {
                    drawCircle(Color(0xFFE74C3C),
                        radius = 6f,
                        center = Offset(px + w - 8f, py + 8f))
                    drawContext.canvas.nativeCanvas.apply {
                        drawText("↻",
                            px + w - 8f, py + 14f,
                            android.graphics.Paint().apply {
                                textSize  = 12f
                                this.color = android.graphics.Color.WHITE
                                textAlign = android.graphics.Paint.Align.CENTER
                            })
                    }
                }

                // خط الطي
                drawLine(color.copy(alpha = 0.6f),
                    Offset(px + w * 0.15f, py + h * 0.5f),
                    Offset(px + w * 0.85f, py + h * 0.5f),
                    strokeWidth = 1f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 3f)))

                // نص
                drawContext.canvas.nativeCanvas.apply {
                    drawText(placed.piece.nameAr,
                        px + w / 2f, py + h / 2f - 6f,
                        android.graphics.Paint().apply {
                            textSize = 12f
                            this.color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = isSelected
                        })
                    drawText("%.0f×%.0f سم".format(
                        placed.piece.widthCm, placed.piece.heightCm),
                        px + w / 2f, py + h / 2f + 8f,
                        android.graphics.Paint().apply {
                            textSize  = 9f
                            this.color = android.graphics.Color.DKGRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                        })
                }
            }
        }
    }
}
