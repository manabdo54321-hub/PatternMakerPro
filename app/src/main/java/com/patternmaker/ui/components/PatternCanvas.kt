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
import com.patternmaker.domain.engine.CurveEngine
import com.patternmaker.domain.model.BezierCurve
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.Point

private val PIECE_COLORS = listOf(
    Color(0xFF1B4F72), Color(0xFF148F77),
    Color(0xFF7D3C98), Color(0xFFB7950B),
    Color(0xFFCB4335),
)

@Composable
fun PatternCanvas(
    pieces: List<PatternPiece>,
    modifier: Modifier = Modifier
) {
    val CM = 5f  // 1سم = 5px

    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(20f, 20f)) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale  = (scale * zoomChange).coerceIn(0.3f, 8f)
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
            var cx = 10f
            var cy = 10f
            var rowH = 0f

            pieces.forEachIndexed { i, piece ->
                val color = PIECE_COLORS[i % PIECE_COLORS.size]
                val pw = piece.widthCm * CM
                val ph = piece.heightCm * CM

                if (cx + pw > 600f && cx > 10f) {
                    cx = 10f; cy += rowH + 20f; rowH = 0f
                }

                drawPiece(piece, Offset(cx, cy), color, CM)
                cx += pw + 15f
                if (ph > rowH) rowH = ph
            }
        }
    }
}

private fun DrawScope.drawPiece(
    piece: PatternPiece,
    pos: Offset,
    color: Color,
    CM: Float
) {
    val path = Path()

    if (piece.points.isNotEmpty()) {
        // رسم الحدود المستقيمة
        path.moveTo(pos.x + piece.points[0].x * CM, pos.y + piece.points[0].y * CM)
        piece.points.drop(1).forEach { pt ->
            path.lineTo(pos.x + pt.x * CM, pos.y + pt.y * CM)
        }
        path.close()
    }

    // رسم المنحنيات فوق الحدود
    val curvePath = Path()
    piece.curves.forEach { curve ->
        val pts = CurveEngine.curvePoints(curve, steps = 40)
        if (pts.isNotEmpty()) {
            curvePath.moveTo(pos.x + pts[0].x * CM, pos.y + pts[0].y * CM)
            pts.drop(1).forEach { pt ->
                curvePath.lineTo(pos.x + pt.x * CM, pos.y + pt.y * CM)
            }
        }
    }

    // تلوين القطعة
    drawPath(path, color = color.copy(alpha = 0.2f))

    // حدود مستقيمة
    drawPath(path, color = color, style = Stroke(width = 2f,
        cap = StrokeCap.Round, join = StrokeJoin.Round))

    // المنحنيات بخط مميز
    drawPath(curvePath, color = color.copy(alpha = 0.9f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(4f)))

    // هامش الخياطة (خط منقط)
    val saPath = Path()
    val saCM = 1.5f * CM
    saPath.addRect(
        androidx.compose.ui.geometry.Rect(
            left   = pos.x + saCM,
            top    = pos.y + saCM,
            right  = pos.x + piece.widthCm * CM - saCM,
            bottom = pos.y + piece.heightCm * CM - saCM
        )
    )
    drawPath(saPath, color = color.copy(alpha = 0.4f),
        style = Stroke(width = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f))))

    // خط الطي (Grain Line) ←→
    val midY = pos.y + piece.heightCm * CM * 0.5f
    drawLine(color, Offset(pos.x + piece.widthCm * CM * 0.2f, midY),
        Offset(pos.x + piece.widthCm * CM * 0.8f, midY),
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 4f)))

    // اسم القطعة
    drawContext.canvas.nativeCanvas.apply {
        drawText("${piece.nameAr} ×${piece.quantity}",
            pos.x + piece.widthCm * CM / 2f,
            pos.y + piece.heightCm * CM / 2f - 8f,
            android.graphics.Paint().apply {
                textSize = 14f; this.color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true
            })
        drawText("%.1f × %.1f سم".format(piece.widthCm, piece.heightCm),
            pos.x + piece.widthCm * CM / 2f,
            pos.y + piece.heightCm * CM / 2f + 10f,
            android.graphics.Paint().apply {
                textSize = 11f; this.color = android.graphics.Color.DKGRAY
                textAlign = android.graphics.Paint.Align.CENTER
            })
    }
}
