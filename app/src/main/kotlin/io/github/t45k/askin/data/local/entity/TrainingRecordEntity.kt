package io.github.t45k.askin.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "training_records",
    indices = [
        Index(value = ["date", "category_name", "exercise_name"], unique = true),
    ],
)
data class TrainingRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,
    @ColumnInfo(name = "category_display_order")
    val categoryDisplayOrder: Int,
    @ColumnInfo(name = "exercise_display_order")
    val exerciseDisplayOrder: Int,
    val reps: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
