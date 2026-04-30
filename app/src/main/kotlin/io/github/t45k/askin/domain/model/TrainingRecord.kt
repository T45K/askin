package io.github.t45k.askin.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class TrainingRecord(
    val id: Long,
    val date: LocalDate,
    val categoryName: String,
    val exerciseName: String,
    val reps: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
