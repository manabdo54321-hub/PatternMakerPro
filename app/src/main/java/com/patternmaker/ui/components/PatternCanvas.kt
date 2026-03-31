package com.patternmaker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.Point

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
    var scale by remember { mutableStateOf(3f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 10f)
        offset += panChange
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .transformable(state = transformState)
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {
            // رسم شبكة خلفية
            drawGrid(this)

            // رسم كل قطعة
            var currentX = 10f
            pieces.forEachIndexed { index, piece ->
                val color = PIECE_COLORS[index % PIECE_COLORS.size]
                drawPiece(piece, Offset(currentX, 10f), color)
                currentX += piece.widthCm + 5f
            }
        }
    }
}

private fun DrawScope.drawGrid(scope: DrawScope) {
    val gridSize = 10f
    val gridColor = Color(0xFFCCCCCC)
    var x = 0f
    while (x < 300f) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, 300f), strokeWidth = 0.2f)
        x += gridSize
    }
    var y = 0f
    while (y < 300f) {
        drawLine(gridColor, Offset(0f, y), Offset(300f, y), strokeWidth = 0.2f)
        y += gridSize
    }
}

private fun DrawScope.drawPiece(
    piece: PatternPiece,
    position: Offset,
    color: Color
) {
    if (piece.points.isEmpty()) return

    val path = Path()
    val firstPoint = piece.points.first()
    path.moveTo(position.x + firstPoint.x, position.y + firstPoint.y)

    // رسم الخطوط المستقيمة
    piece.points.drop(1).forEach { point ->
        path.lineTo(position.x + point.x, position.y + point.y)
    }

    // رسم منحنيات Bezier
    piece.curves.forEach { curve ->
        path.moveTo(position.x + curve.start.x, position.y + curve.start.y)
        path.cubicTo(
            position.x + curve.control1.x, position.y + curve.control1.y,
            position.x + curve.control2.x, position.y + curve.control2.y,
            position.x + curve.end.x,      position.y + curve.end.y
        )
    }

    path.close()

    // تلوين القطعة
    drawPath(path, color = color.copy(alpha = 0.25f))

    // حدود القطعة
    drawPath(
        path,
        color = color,
        style = Stroke(width = 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // اسم القطعة في المنتصف
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize    = 5f
            this.color  = android.graphics.Color.BLACK
            textAlign   = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        drawText(
            "${piece.nameAr} ×${piece.quantity}",
            position.x + piece.widthCm / 2f,
            position.y + piece.heightCm / 2f,
            paint
        )
    }

    // القياسات
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize   = 3.5f
            this.color = android.graphics.Color.DKGRAY
            textAlign  = android.graphics.Paint.Align.CENTER
        }
        drawText(
            "%.1f × %.1f سم".format(piece.widthCm, piece.heightCm),
            position.x + piece.widthCm / 2f,
            position.y + piece.heightCm / 2f + 7f,
            paint
        )
    }

    // خط الطي (Grain Line)
    drawLine(
        color = color.copy(alpha = 0.7f),
        start = Offset(position.x + piece.widthCm * 0.3f, position.y + piece.heightCm * 0.4f),
        end   = Offset(position.x + piece.widthCm * 0.7f, position.y + piece.heightCm * 0.4f),
        strokeWidth = 0.5f,
        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(2f, 1f))
    )
}
