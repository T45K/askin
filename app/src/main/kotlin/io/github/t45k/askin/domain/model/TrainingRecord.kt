package io.github.t45k.askin.domain.model

import java.time.Instant
import java.time.LocalDate

data class TrainingRecord(
    val id: Long,
    val date: LocalDate,
    val categoryName: String,
    val exerciseName: String,
    val reps: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
