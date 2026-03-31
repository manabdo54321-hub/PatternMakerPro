package com.patternmaker.domain.model

data class Point(val x: Float, val y: Float)

data class BezierCurve(
    val start: Point,
    val control1: Point,
    val control2: Point,
    val end: Point
)

data class PatternPiece(
    val id: String,
    val nameAr: String,
    val points: List<Point>,
    val curves: List<BezierCurve> = emptyList(),
    val quantity: Int = 1,
    val canRotate: Boolean = true,
    val widthCm: Float = 0f,
    val heightCm: Float = 0f
)
