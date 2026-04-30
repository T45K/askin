package io.github.t45k.askin.domain.model

import kotlinx.datetime.LocalDate

data class DailyTrainingSummary(
    val date: LocalDate,
    val totalReps: Int,
    val records: List<DailyTrainingRecord>,
)

data class DailyTrainingRecord(
    val exerciseName: String,
    val categoryName: String,
    val reps: Int,
)
