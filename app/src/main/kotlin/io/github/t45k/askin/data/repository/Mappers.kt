package io.github.t45k.askin.data.repository

import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.DailyExerciseRecordEntity
import io.github.t45k.askin.data.local.entity.DailyTotalEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.data.local.entity.TrainingRecordEntity
import io.github.t45k.askin.domain.model.Category
import io.github.t45k.askin.domain.model.DailyTrainingRecord
import io.github.t45k.askin.domain.model.DailyTotal
import io.github.t45k.askin.domain.model.Exercise
import io.github.t45k.askin.domain.model.TrainingRecord

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
    displayOrder = displayOrder,
    isActive = isActive,
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    categoryId = categoryId,
    name = name,
    description = description,
    displayOrder = displayOrder,
    isActive = isActive,
)

fun TrainingRecordEntity.toDomain(): TrainingRecord = TrainingRecord(
    id = id,
    date = date,
    categoryName = categoryName,
    exerciseName = exerciseName,
    reps = reps,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DailyExerciseRecordEntity.toDomain(): DailyTrainingRecord = DailyTrainingRecord(
    exerciseName = exerciseName,
    categoryName = categoryName,
    reps = reps,
)

fun DailyTotalEntity.toDomain(): DailyTotal = DailyTotal(
    date = date,
    totalReps = totalReps,
)
