package com.example.myapplication.preferences

import kotlinx.serialization.Serializable

@Serializable
data class QuizMarkTypeSetting(
    val id: String,
    val name: String,
    val defaultPoints: Double,
    val contributesToTotal: Boolean,
    val isExtraCredit: Boolean
)
