package com.patternmaker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.patternmaker.domain.model.PatternPiece

private val PIECE_COLORS = listOf(
    Color(0xFF1B4F72),
    Color(0xFF148F77),
    Color(0xFF7D3C98),
    Color(0xFFB7950B),
    Color(0xFFCB4335),
)

@Composable
fun PatternCanvas(
    pieces: List<PatternPiece>,
    modifier: Modifier = Modifier
) {
    // SCALE: 1 سم = 5 بكسل على الشاشة
    val CM_TO_PX = 5f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(20f, 20f)) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.3f, 5f)
        offset += panChange
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .transformable(state = transformState)
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {

            var currentX = 0f
            var currentY = 0f
            var maxRowHeight = 0f

            pieces.forEachIndexed { index, piece ->
                val color = PIECE_COLORS[index % PIECE_COLORS.size]
                val pw = piece.widthCm * CM_TO_PX
                val ph = piece.heightCm * CM_TO_PX

                // لو القطعة هتطلع برة الشاشة — نزل لصف جديد
                if (currentX + pw > 400f && currentX > 0f) {
                    currentX = 0f
                    currentY += maxRowHeight + 20f
                    maxRowHeight = 0f
                }

                val pos = Offset(currentX, currentY)

                // رسم القطعة
                drawRect(
                    color   = color.copy(alpha = 0.3f),
                    topLeft = pos,
                    size    = Size(pw, ph)
                )
                drawRect(
                    color   = color,
                    topLeft = pos,
                    size    = Size(pw, ph),
                    style   = Stroke(width = 2f)
                )

                // اسم القطعة
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize       = 14f
                        this.color     = android.graphics.Color.BLACK
                        textAlign      = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(
                        "${piece.nameAr} ×${piece.quantity}",
                        pos.x + pw / 2f,
                        pos.y + ph / 2f,
                        paint
                    )
                    val paint2 = android.graphics.Paint().apply {
                        textSize   = 11f
                        this.color = android.graphics.Color.DKGRAY
                        textAlign  = android.graphics.Paint.Align.CENTER
                    }
                    drawText(
                        "%.1f × %.1f سم".format(piece.widthCm, piece.heightCm),
                        pos.x + pw / 2f,
                        pos.y + ph / 2f + 18f,
                        paint2
                    )
                }

                // خط الطي
                drawLine(
                    color       = color,
                    start       = Offset(pos.x + pw * 0.2f, pos.y + ph * 0.5f),
                    end         = Offset(pos.x + pw * 0.8f, pos.y + ph * 0.5f),
                    strokeWidth = 1.5f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )

                currentX += pw + 15f
                if (ph > maxRowHeight) maxRowHeight = ph
            }
        }
    }
}
