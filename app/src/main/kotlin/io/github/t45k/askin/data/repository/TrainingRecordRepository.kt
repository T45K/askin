package io.github.t45k.askin.data.repository

import io.github.t45k.askin.data.local.dao.TrainingRecordDao
import io.github.t45k.askin.domain.model.DailyTrainingSummary
import io.github.t45k.askin.domain.model.DailyTotal
import io.github.t45k.askin.domain.model.TrainingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

class TrainingRecordRepository(
    private val trainingRecordDao: TrainingRecordDao,
    private val masterRepository: MasterRepository,
    private val clock: Clock = Clock.System,
) {
    suspend fun addReps(date: LocalDate, exerciseId: Long, reps: Int) {
        require(reps > 0) { "Reps must be greater than zero." }
        require(exerciseId > 0) { "Exercise id must be greater than zero." }
        val exercise = requireNotNull(masterRepository.getExercise(exerciseId)) { "Exercise was not found." }
        val category = requireNotNull(masterRepository.getCategory(exercise.categoryId)) { "Category was not found." }

        trainingRecordDao.addReps(
            date = date,
            categoryName = category.name,
            exerciseName = exercise.name,
            categoryDisplayOrder = category.displayOrder,
            exerciseDisplayOrder = exercise.displayOrder,
            additionalReps = reps,
            now = clock.now(),
        )
    }

    suspend fun getRecord(date: LocalDate, categoryName: String, exerciseName: String): TrainingRecord? = trainingRecordDao
        .getRecord(date, categoryName, exerciseName)
        ?.toDomain()

    fun observeDailySummary(date: LocalDate): Flow<DailyTrainingSummary> = trainingRecordDao
        .observeDailyExerciseRecords(date)
        .map { records ->
            DailyTrainingSummary(
                date = date,
                totalReps = records.sumOf { it.reps },
                records = records.map { it.toDomain() },
            )
        }

    fun observeDailyTotals(): Flow<List<DailyTotal>> = trainingRecordDao
        .observeDailyTotals()
        .map { totals -> totals.map { it.toDomain() } }

    suspend fun getDailySummary(date: LocalDate): DailyTrainingSummary {
        val records = trainingRecordDao.getDailyExerciseRecords(date)
        return DailyTrainingSummary(
            date = date,
            totalReps = records.sumOf { it.reps },
            records = records.map { it.toDomain() },
        )
    }
}
