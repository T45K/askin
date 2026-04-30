package io.github.t45k.askin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY display_order, id")
    fun observeActiveExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY display_order, id")
    suspend fun getActiveExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE category_id = :categoryId AND is_active = 1 ORDER BY display_order, id")
    suspend fun getActiveExercisesByCategory(categoryId: Long): List<ExerciseEntity>

    @Query("SELECT * FROM exercises ORDER BY category_id, display_order, id")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM exercises WHERE category_id = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Long)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
