package io.github.t45k.askin.data.repository

import androidx.room.withTransaction
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.dao.CategoryDao
import io.github.t45k.askin.data.local.dao.ExerciseDao
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.domain.model.Category
import io.github.t45k.askin.domain.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MasterRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val exerciseDao: ExerciseDao,
) {
    fun observeActiveCategories(): Flow<List<Category>> = categoryDao
        .observeActiveCategories()
        .map { categories -> categories.map { it.toDomain() } }

    fun observeActiveExercises(): Flow<List<Exercise>> = exerciseDao
        .observeActiveExercises()
        .map { exercises -> exercises.map { it.toDomain() } }

    suspend fun getActiveCategories(): List<Category> = categoryDao
        .getActiveCategories()
        .map { it.toDomain() }

    suspend fun getActiveExercises(): List<Exercise> = exerciseDao
        .getActiveExercises()
        .map { it.toDomain() }

    suspend fun addCategory(name: String, description: String, displayOrder: Int): Long {
        validateName(name, "Category name")
        return categoryDao.insert(
            CategoryEntity(
                name = name.trim(),
                description = description.trim(),
                displayOrder = displayOrder,
                isActive = true,
            ),
        )
    }

    suspend fun updateCategory(id: Long, name: String, description: String, displayOrder: Int) {
        validateId(id, "Category id")
        validateName(name, "Category name")
        val current = requireNotNull(categoryDao.getById(id)) { "Category was not found." }
        categoryDao.update(
            current.copy(
                name = name.trim(),
                description = description.trim(),
                displayOrder = displayOrder,
            ),
        )
    }

    suspend fun deactivateCategory(id: Long) {
        validateId(id, "Category id")
        database.withTransaction {
            categoryDao.deactivate(id)
            exerciseDao.deactivateByCategoryId(id)
        }
    }

    suspend fun addExercise(name: String, description: String, categoryId: Long, displayOrder: Int): Long {
        validateName(name, "Exercise name")
        validateId(categoryId, "Category id")
        requireNotNull(categoryDao.getById(categoryId)) { "Category was not found." }
        return exerciseDao.insert(
            ExerciseEntity(
                name = name.trim(),
                description = description.trim(),
                categoryId = categoryId,
                displayOrder = displayOrder,
                isActive = true,
            ),
        )
    }

    suspend fun updateExercise(id: Long, name: String, description: String, categoryId: Long, displayOrder: Int) {
        validateId(id, "Exercise id")
        validateName(name, "Exercise name")
        validateId(categoryId, "Category id")
        requireNotNull(categoryDao.getById(categoryId)) { "Category was not found." }
        val current = requireNotNull(exerciseDao.getById(id)) { "Exercise was not found." }
        exerciseDao.update(
            current.copy(
                name = name.trim(),
                description = description.trim(),
                categoryId = categoryId,
                displayOrder = displayOrder,
            ),
        )
    }

    suspend fun deactivateExercise(id: Long) {
        validateId(id, "Exercise id")
        exerciseDao.deactivate(id)
    }

    private fun validateName(name: String, label: String) {
        require(name.isNotBlank()) { "$label is required." }
    }

    private fun validateId(id: Long, label: String) {
        require(id > 0) { "$label must be greater than zero." }
    }
}
