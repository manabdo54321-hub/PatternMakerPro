package com.patternmaker.domain.engine

import com.patternmaker.domain.model.BezierCurve
import com.patternmaker.domain.model.PathSegment
import com.patternmaker.domain.model.Point
import kotlin.math.sqrt

object CurveEngine {

    fun frontCrotchCurve(fromPoint: Point, crotchDepth: Float, panelWidth: Float): PathSegment.CurveTo =
        PathSegment.CurveTo(
            control1 = Point(fromPoint.x + panelWidth * 0.04f, fromPoint.y - crotchDepth * 0.35f),
            control2 = Point(fromPoint.x + panelWidth * 0.18f, fromPoint.y - crotchDepth * 0.85f),
            end      = Point(fromPoint.x + panelWidth * 0.25f, fromPoint.y - crotchDepth)
        )

    fun backCrotchCurve(fromPoint: Point, crotchDepth: Float, panelWidth: Float): PathSegment.CurveTo {
        val ext = panelWidth * 0.08f
        return PathSegment.CurveTo(
            control1 = Point(fromPoint.x - ext,                fromPoint.y - crotchDepth * 0.30f),
            control2 = Point(fromPoint.x + panelWidth * 0.15f, fromPoint.y - crotchDepth * 0.90f),
            end      = Point(fromPoint.x + panelWidth * 0.35f, fromPoint.y - crotchDepth)
        )
    }

    fun frontWaistCurve(fromPoint: Point, panelWidth: Float, waistDip: Float): PathSegment.CurveTo =
        PathSegment.CurveTo(
            control1 = Point(fromPoint.x + panelWidth * 0.30f, fromPoint.y + waistDip * 0.5f),
            control2 = Point(fromPoint.x + panelWidth * 0.70f, fromPoint.y + waistDip * 0.1f),
            end      = Point(fromPoint.x + panelWidth,          fromPoint.y)
        )

    fun backWaistCurve(fromPoint: Point, panelWidth: Float, waistRise: Float): PathSegment.CurveTo =
        PathSegment.CurveTo(
            control1 = Point(fromPoint.x + panelWidth * 0.25f, fromPoint.y - waistRise * 0.6f),
            control2 = Point(fromPoint.x + panelWidth * 0.75f, fromPoint.y + waistRise * 0.2f),
            end      = Point(fromPoint.x + panelWidth,          fromPoint.y + waistRise)
        )

    fun evaluateCubicBezier(curve: BezierCurve, t: Float): Point {
        val mt = 1f - t
        return Point(
            x = mt*mt*mt * curve.start.x + 3f*mt*mt*t * curve.control1.x +
                3f*mt*t*t * curve.control2.x + t*t*t * curve.end.x,
            y = mt*mt*mt * curve.start.y + 3f*mt*mt*t * curve.control1.y +
                3f*mt*t*t * curve.control2.y + t*t*t * curve.end.y
        )
    }

    fun curveLength(curve: BezierCurve, steps: Int = 50): Float {
        var length = 0f
        var prev   = evaluateCubicBezier(curve, 0f)
        for (i in 1..steps) {
            val curr = evaluateCubicBezier(curve, i.toFloat() / steps)
            val dx   = curr.x - prev.x
            val dy   = curr.y - prev.y
            length  += sqrt(dx*dx + dy*dy)
            prev     = curr
        }
        return length
    }

    fun curvePoints(curve: BezierCurve, steps: Int = 30): List<Point> =
        (0..steps).map { evaluateCubicBezier(curve, it.toFloat() / steps) }
}
