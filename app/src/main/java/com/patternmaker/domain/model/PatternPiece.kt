package com.patternmaker.domain.model

data class Point(val x: Float, val y: Float) {
    operator fun plus(other: Point)  = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun times(t: Float)     = Point(x * t, y * t)
    fun distanceTo(other: Point)     = kotlin.math.sqrt(
        (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)
    )
}

sealed class PathSegment {
    data class LineTo(val end: Point) : PathSegment()
    data class CurveTo(
        val control1: Point,
        val control2: Point,
        val end: Point
    ) : PathSegment()
}

data class PatternPiece(
    val id: String,
    val nameAr: String,
    val startPoint: Point,
    val segments: List<PathSegment>,
    val grainLineAngle: Float = 0f,
    val seamAllowance: Float = 1.5f,
    val quantity: Int = 1,
    val canRotate: Boolean = true,
    val widthCm: Float = 0f,
    val heightCm: Float = 0f
) {
    fun toPoints(steps: Int = 20): List<Point> {
        val pts = mutableListOf(startPoint)
        var current = startPoint
        for (seg in segments) {
            when (seg) {
                is PathSegment.LineTo -> {
                    pts.add(seg.end)
                    current = seg.end
                }
                is PathSegment.CurveTo -> {
                    for (i in 1..steps) {
                        val t  = i.toFloat() / steps
                        val mt = 1f - t
                        pts.add(Point(
                            x = mt*mt*mt * current.x +
                                3f*mt*mt*t * seg.control1.x +
                                3f*mt*t*t  * seg.control2.x +
                                t*t*t      * seg.end.x,
                            y = mt*mt*mt * current.y +
                                3f*mt*mt*t * seg.control1.y +
                                3f*mt*t*t  * seg.control2.y +
                                t*t*t      * seg.end.y
                        ))
                    }
                    current = seg.end
                }
            }
        }
        return pts
    }

    fun realArea(): Float {
        val pts = toPoints()
        if (pts.size < 3) return 0f
        var area = 0f
        for (i in pts.indices) {
            val j = (i + 1) % pts.size
            area += pts[i].x * pts[j].y
            area -= pts[j].x * pts[i].y
        }
        return kotlin.math.abs(area) / 2f
    }

    fun boundingBox(): Pair<Point, Point> {
        val pts = toPoints()
        val minX = pts.minOf { it.x }
        val minY = pts.minOf { it.y }
        val maxX = pts.maxOf { it.x }
        val maxY = pts.maxOf { it.y }
        return Pair(Point(minX, minY), Point(maxX, maxY))
    }
}

data class BezierCurve(
    val start: Point,
    val control1: Point,
    val control2: Point,
    val end: Point
)
