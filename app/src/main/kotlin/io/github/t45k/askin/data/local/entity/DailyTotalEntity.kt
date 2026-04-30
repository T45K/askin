package io.github.t45k.askin.data.local.entity

import androidx.room.ColumnInfo
import java.time.LocalDate

data class DailyTotalEntity(
    val date: LocalDate,
    @ColumnInfo(name = "total_reps")
    val totalReps: Int,
)
