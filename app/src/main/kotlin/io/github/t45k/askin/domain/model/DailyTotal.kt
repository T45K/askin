package io.github.t45k.askin.domain.model

import kotlinx.datetime.LocalDate

data class DailyTotal(
    val date: LocalDate,
    val totalReps: Int,
)
