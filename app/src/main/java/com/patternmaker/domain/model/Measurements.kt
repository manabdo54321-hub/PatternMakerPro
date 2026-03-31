package com.patternmaker.domain.model

data class Measurements(
    val id: Long = 0,
    val clientName: String = "",
    val waist: Float = 0f,
    val hip: Float = 0f,
    val length: Float = 0f,
    val legWidth: Float = 0f,
    val rise: Float = 0f,
    val seamAllowance: Float = 1.5f,
    val ease: Float = 6f
)
