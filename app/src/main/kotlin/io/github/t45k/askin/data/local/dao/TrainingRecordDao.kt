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
import java.time.Instant
import java.time.LocalDate

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
          AND exercise_id = :exerciseId
        """,
    )
    suspend fun incrementReps(
        date: LocalDate,
        exerciseId: Long,
        additionalReps: Int,
        updatedAt: Instant,
    ): Int

    @Transaction
    suspend fun addReps(
        date: LocalDate,
        exerciseId: Long,
        additionalReps: Int,
        now: Instant,
    ) {
        val insertedId = insert(
            TrainingRecordEntity(
                date = date,
                exerciseId = exerciseId,
                reps = additionalReps,
                createdAt = now,
                updatedAt = now,
            ),
        )

        if (insertedId == -1L) {
            incrementReps(
                date = date,
                exerciseId = exerciseId,
                additionalReps = additionalReps,
                updatedAt = now,
            )
        }
    }

    @Query("SELECT * FROM training_records WHERE date = :date AND exercise_id = :exerciseId")
    suspend fun getRecord(date: LocalDate, exerciseId: Long): TrainingRecordEntity?

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
               tr.exercise_id AS exercise_id,
               e.name AS exercise_name,
               c.name AS category_name,
               tr.reps AS reps
        FROM training_records AS tr
        INNER JOIN exercises AS e ON e.id = tr.exercise_id
        INNER JOIN categories AS c ON c.id = e.category_id
        WHERE tr.date = :date
        ORDER BY c.display_order, e.display_order, e.id
        """,
    )
    fun observeDailyExerciseRecords(date: LocalDate): Flow<List<DailyExerciseRecordEntity>>

    @Query(
        """
        SELECT tr.date AS date,
               tr.exercise_id AS exercise_id,
               e.name AS exercise_name,
               c.name AS category_name,
               tr.reps AS reps
        FROM training_records AS tr
        INNER JOIN exercises AS e ON e.id = tr.exercise_id
        INNER JOIN categories AS c ON c.id = e.category_id
        WHERE tr.date = :date
        ORDER BY c.display_order, e.display_order, e.id
        """,
    )
    suspend fun getDailyExerciseRecords(date: LocalDate): List<DailyExerciseRecordEntity>
}
