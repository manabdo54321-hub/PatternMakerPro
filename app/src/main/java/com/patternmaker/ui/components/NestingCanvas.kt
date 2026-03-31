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
import com.patternmaker.domain.model.PathSegment
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.Point

private val NESTING_COLORS = listOf(
    Color(0xFF1B4F72), Color(0xFF148F77),
    Color(0xFF7D3C98), Color(0xFFB7950B),
    Color(0xFFCB4335), Color(0xFF1A5276),
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
    var scale  by remember { mutableStateOf(0.9f) }
    var offset by remember { mutableStateOf(Offset(10f, 10f)) }
    val ts = rememberTransformableState { zc, pc, _ ->
        scale = (scale * zc).coerceIn(0.15f, 10f)
        offset += pc
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF888888))
            .transformable(state = ts)
            .pointerInput(layout, scale, offset) {
                detectTapGestures { tap ->
                    val cx = (tap.x - offset.x) / scale / CM
                    val cy = (tap.y - offset.y) / scale / CM
                    var found = -1
                    layout.placedPieces.forEachIndexed { i, placed ->
                        val w = if (placed.rotation == 90f) placed.piece.heightCm else placed.piece.widthCm
                        val h = if (placed.rotation == 90f) placed.piece.widthCm  else placed.piece.heightCm
                        if (cx >= placed.x && cx <= placed.x + w &&
                            cy >= placed.y && cy <= placed.y + h) found = i
                    }
                    onPieceSelected(found)
                }
            }
            .pointerInput(selectedIndex, scale) {
                detectDragGestures(onDragStart = {}, onDrag = { change, drag ->
                    change.consume()
                    if (selectedIndex >= 0)
                        onPieceMoved(selectedIndex, drag.x / scale / CM, drag.y / scale / CM)
                })
            }
    ) {
        withTransform({ translate(offset.x, offset.y); scale(scale, scale) }) {
            val fw = layout.fabricWidth  * CM
            val fl = layout.fabricLength * CM

            // خلفية القماش
            drawRect(Color(0xFFFAF5EC), size = Size(fw, fl))

            // شبكة القياس
            var gx = 0f
            while (gx <= layout.fabricWidth) {
                val thick = gx.toInt() % 50 == 0
                drawLine(if (thick) Color(0xFFAAAAAA) else Color(0xFFDDDDDD),
                    Offset(gx * CM, 0f), Offset(gx * CM, fl),
                    if (thick) 1f else 0.4f)
                gx += 10f
            }
            var gy = 0f
            while (gy <= layout.fabricLength) {
                val thick = gy.toInt() % 50 == 0
                drawLine(if (thick) Color(0xFFAAAAAA) else Color(0xFFDDDDDD),
                    Offset(0f, gy * CM), Offset(fw, gy * CM),
                    if (thick) 1f else 0.4f)
                gy += 10f
            }

            // حدود القماش
            drawRect(Color(0xFF222222), size = Size(fw, fl), style = Stroke(width = 2.5f))

            // القطع بشكلها الحقيقي
            layout.placedPieces.forEachIndexed { i, placed ->
                val color  = NESTING_COLORS[i % NESTING_COLORS.size]
                val isSel  = i == selectedIndex
                val pos    = Offset(placed.x * CM, placed.y * CM)
                val pivotX = pos.x + placed.piece.widthCm  * CM / 2f
                val pivotY = pos.y + placed.piece.heightCm * CM / 2f

                withTransform({
                    if (placed.rotation != 0f) rotate(placed.rotation, Offset(pivotX, pivotY))
                }) {
                    val path = buildPiecePath(placed.piece, pos, CM)

                    // ظل للمحدد
                    if (isSel) drawPath(
                        buildPiecePath(placed.piece, Offset(pos.x + 4f, pos.y + 4f), CM),
                        Color(0x30000000)
                    )

                    // تلوين داخلي
                    drawPath(path, color = color.copy(alpha = if (isSel) 0.50f else 0.28f))

                    // حد القطعة
                    drawPath(path,
                        color = if (isSel) Color(0xFFE74C3C) else color,
                        style = Stroke(
                            width = if (isSel) 3f else 1.8f,
                            cap   = StrokeCap.Round,
                            join  = StrokeJoin.Round
                        ))

                    // هامش الخياطة المنقط
                    val sa = placed.piece.seamAllowance * CM
                    val (mn, mx) = placed.piece.boundingBox()
                    drawPath(
                        Path().apply {
                            addRect(androidx.compose.ui.geometry.Rect(
                                pos.x + mn.x * CM + sa, pos.y + mn.y * CM + sa,
                                pos.x + mx.x * CM - sa, pos.y + mx.y * CM - sa
                            ))
                        },
                        color.copy(alpha = 0.35f),
                        style = Stroke(width = 0.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
                    )

                    // خط الطي
                    val cx  = pos.x + placed.piece.widthCm  * CM / 2f
                    val cy  = pos.y + placed.piece.heightCm * CM / 2f
                    val len = minOf(placed.piece.widthCm, placed.piece.heightCm) * CM * 0.30f
                    drawLine(color.copy(alpha = 0.45f),
                        Offset(cx - len, cy), Offset(cx + len, cy), 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f)))

                    // مقبض التدوير
                    if (isSel) drawCircle(Color(0xFFE74C3C), 7f,
                        Offset(pos.x + placed.piece.widthCm * CM - 8f, pos.y + 8f))

                    // الاسم
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(placed.piece.nameAr, cx, cy - 5f,
                            android.graphics.Paint().apply {
                                textSize = 11f
                                this.color = android.graphics.Color.BLACK
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isSel; isAntiAlias = true
                            })
                        drawText("%.0f×%.0f".format(
                            placed.piece.widthCm, placed.piece.heightCm), cx, cy + 9f,
                            android.graphics.Paint().apply {
                                textSize = 9f
                                this.color = android.graphics.Color.DKGRAY
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            })
                    }
                }
            }
        }
    }
}
