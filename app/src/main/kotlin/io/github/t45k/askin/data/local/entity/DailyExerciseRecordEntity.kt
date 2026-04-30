package io.github.t45k.askin.data.local.entity

import androidx.room.ColumnInfo
import java.time.LocalDate

data class DailyExerciseRecordEntity(
    val date: LocalDate,
    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    val reps: Int,
)
