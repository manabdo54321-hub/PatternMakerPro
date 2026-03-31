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
import com.patternmaker.domain.model.PathSegment
import com.patternmaker.domain.model.PatternPiece
import com.patternmaker.domain.model.Point

private val PIECE_COLORS = listOf(
    Color(0xFF1B4F72), Color(0xFF148F77),
    Color(0xFF7D3C98), Color(0xFFB7950B),
    Color(0xFFCB4335), Color(0xFF1A5276),
    Color(0xFF117A65), Color(0xFF6C3483),
)

@Composable
fun PatternCanvas(pieces: List<PatternPiece>, modifier: Modifier = Modifier) {
    val CM = 6f
    var scale  by remember { mutableStateOf(0.85f) }
    var offset by remember { mutableStateOf(Offset(16f, 16f)) }
    val ts = rememberTransformableState { zc, pc, _ ->
        scale = (scale * zc).coerceIn(0.2f, 10f)
        offset += pc
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0EDE8))
            .transformable(state = ts)
    ) {
        withTransform({ translate(offset.x, offset.y); scale(scale, scale) }) {
            var cx = 12f; var cy = 12f; var rowH = 0f
            pieces.forEachIndexed { i, piece ->
                val color = PIECE_COLORS[i % PIECE_COLORS.size]
                val pw = piece.widthCm * CM
                val ph = piece.heightCm * CM
                if (cx + pw > 800f && cx > 12f) {
                    cx = 12f; cy += rowH + 20f; rowH = 0f
                }
                drawPatternPiece(piece, Offset(cx, cy), color, CM)
                cx += pw + 20f
                if (ph > rowH) rowH = ph
            }
        }
    }
}

private fun DrawScope.drawPatternPiece(
    piece: PatternPiece, pos: Offset, color: Color, CM: Float
) {
    val path = buildPiecePath(piece, pos, CM)

    // التلوين الداخلي
    drawPath(path, color = color.copy(alpha = 0.18f))

    // حد القطعة الحقيقي
    drawPath(path, color = color,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // خط هامش الخياطة المنقط
    val sa = piece.seamAllowance * CM
    val (mn, mx) = piece.boundingBox()
    val saPath = Path().apply {
        addRect(androidx.compose.ui.geometry.Rect(
            pos.x + mn.x * CM + sa, pos.y + mn.y * CM + sa,
            pos.x + mx.x * CM - sa, pos.y + mx.y * CM - sa
        ))
    }
    drawPath(saPath, color.copy(alpha = 0.4f),
        style = Stroke(width = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f))))

    // خط الطي مع سهمين
    val cx  = pos.x + piece.widthCm * CM / 2f
    val cy  = pos.y + piece.heightCm * CM / 2f
    val len = minOf(piece.widthCm, piece.heightCm) * CM * 0.35f
    val arr = 5f
    if (piece.grainLineAngle == 90f) {
        val gs = Offset(cx, cy - len); val ge = Offset(cx, cy + len)
        drawLine(color.copy(alpha = 0.5f), gs, ge, 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
        drawLine(color.copy(alpha = 0.5f), gs, Offset(cx - arr, cy - len + arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), gs, Offset(cx + arr, cy - len + arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), ge, Offset(cx - arr, cy + len - arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), ge, Offset(cx + arr, cy + len - arr), 1.5f)
    } else {
        val gs = Offset(cx - len, cy); val ge = Offset(cx + len, cy)
        drawLine(color.copy(alpha = 0.5f), gs, ge, 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
        drawLine(color.copy(alpha = 0.5f), gs, Offset(cx - len + arr, cy - arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), gs, Offset(cx - len + arr, cy + arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), ge, Offset(cx + len - arr, cy - arr), 1.5f)
        drawLine(color.copy(alpha = 0.5f), ge, Offset(cx + len - arr, cy + arr), 1.5f)
    }

    // الاسم والأبعاد والمساحة
    drawContext.canvas.nativeCanvas.apply {
        drawText("${piece.nameAr} ×${piece.quantity}", cx, cy - 10f,
            android.graphics.Paint().apply {
                textSize = 15f; this.color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true; isAntiAlias = true
            })
        drawText("%.1f × %.1f سم".format(piece.widthCm, piece.heightCm), cx, cy + 8f,
            android.graphics.Paint().apply {
                textSize = 11f; this.color = android.graphics.Color.DKGRAY
                textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            })
        val area = piece.realArea()
        if (area > 0f) drawText("%.0f سم²".format(area), cx, cy + 22f,
            android.graphics.Paint().apply {
                textSize = 10f; this.color = android.graphics.Color.DKGRAY
                textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            })
    }
}

fun buildPiecePath(piece: PatternPiece, pos: Offset, CM: Float): Path {
    val path = Path()
    fun p(pt: Point) = Offset(pos.x + pt.x * CM, pos.y + pt.y * CM)
    val s = p(piece.startPoint)
    path.moveTo(s.x, s.y)
    for (seg in piece.segments) when (seg) {
        is PathSegment.LineTo  -> { val e = p(seg.end); path.lineTo(e.x, e.y) }
        is PathSegment.CurveTo -> {
            val c1 = p(seg.control1); val c2 = p(seg.control2); val e = p(seg.end)
            path.cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y)
        }
    }
    path.close()
    return path
}
