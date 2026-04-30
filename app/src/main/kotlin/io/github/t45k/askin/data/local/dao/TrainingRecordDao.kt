package io.github.t45k.askin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.t45k.askin.data.local.entity.DailyTotalEntity
import io.github.t45k.askin.data.local.entity.DailyExerciseRecordEntity
import io.github.t45k.askin.data.local.entity.TrainingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Dao
interface TrainingRecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: TrainingRecordEntity): Long

    @Query(
        """
        UPDATE training_records
        SET reps = reps + :additionalReps,
            updated_at = :updatedAt
        WHERE date = :date
          AND category_name = :categoryName
          AND exercise_name = :exerciseName
        """,
    )
    suspend fun incrementReps(
        date: LocalDate,
        categoryName: String,
        exerciseName: String,
        additionalReps: Int,
        updatedAt: Instant,
    ): Int

    @Transaction
    suspend fun addReps(
        date: LocalDate,
        categoryName: String,
        exerciseName: String,
        categoryDisplayOrder: Int,
        exerciseDisplayOrder: Int,
        additionalReps: Int,
        now: Instant,
    ) {
        val insertedId = insert(
            TrainingRecordEntity(
                date = date,
                categoryName = categoryName,
                exerciseName = exerciseName,
                categoryDisplayOrder = categoryDisplayOrder,
                exerciseDisplayOrder = exerciseDisplayOrder,
                reps = additionalReps,
                createdAt = now,
                updatedAt = now,
            ),
        )

        if (insertedId == -1L) {
            incrementReps(
                date = date,
                categoryName = categoryName,
                exerciseName = exerciseName,
                additionalReps = additionalReps,
                updatedAt = now,
            )
        }
    }

    @Query(
        """
        SELECT *
        FROM training_records
        WHERE date = :date
          AND category_name = :categoryName
          AND exercise_name = :exerciseName
        """,
    )
    suspend fun getRecord(date: LocalDate, categoryName: String, exerciseName: String): TrainingRecordEntity?

    @Query("SELECT COALESCE(SUM(reps), 0) FROM training_records WHERE date = :date")
    suspend fun getTotalRepsForDate(date: LocalDate): Int

    @Query(
        """
        SELECT date AS date,
               SUM(reps) AS total_reps
        FROM training_records
        GROUP BY date
        ORDER BY date DESC
        """,
    )
    fun observeDailyTotals(): Flow<List<DailyTotalEntity>>

    @Query(
        """
        SELECT tr.date AS date,
               tr.exercise_name AS exercise_name,
               tr.category_name AS category_name,
               tr.reps AS reps
        FROM training_records AS tr
        WHERE tr.date = :date
        ORDER BY tr.category_display_order, tr.exercise_display_order, tr.id
        """,
    )
    fun observeDailyExerciseRecords(date: LocalDate): Flow<List<DailyExerciseRecordEntity>>

    @Query(
        """
        SELECT tr.date AS date,
               tr.exercise_name AS exercise_name,
               tr.category_name AS category_name,
               tr.reps AS reps
        FROM training_records AS tr
        WHERE tr.date = :date
        ORDER BY tr.category_display_order, tr.exercise_display_order, tr.id
        """,
    )
    suspend fun getDailyExerciseRecords(date: LocalDate): List<DailyExerciseRecordEntity>
}
