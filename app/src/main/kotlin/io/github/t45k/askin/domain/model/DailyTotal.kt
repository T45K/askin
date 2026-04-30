package io.github.t45k.askin.domain.model

import java.time.LocalDate

data class DailyTotal(
    val date: LocalDate,
    val totalReps: Int,
)
