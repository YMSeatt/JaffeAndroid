package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class StudentLayout(
    val id: Long,
    val x: Float,
    val y: Float
)

@Serializable
data class FurnitureLayout(
    val id: Int,
    val x: Float,
    val y: Float
)

@Serializable
data class LayoutData(
    val students: List<StudentLayout>,
    val furniture: List<FurnitureLayout>
)
