package io.github.t45k.askin.data.local.seed

import androidx.room.withTransaction
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity

class InitialMasterSeeder(
    private val database: AppDatabase,
) {
    suspend fun seedIfNeeded() {
        database.withTransaction {
            if (database.categoryDao().count() > 0 || database.exerciseDao().count() > 0) {
                return@withTransaction
            }

            val categoryIds = database.categoryDao().insertAll(initialCategories)
            val categoryIdByName = initialCategories
                .map { it.name }
                .zip(categoryIds)
                .toMap()

            database.exerciseDao().insertAll(
                initialExercises.flatMap { categoryExercises ->
                    val categoryId = requireNotNull(categoryIdByName[categoryExercises.categoryName])
                    categoryExercises.exerciseNames.mapIndexed { index, name ->
                        ExerciseEntity(
                            categoryId = categoryId,
                            name = name,
                            displayOrder = index + 1,
                            isActive = true,
                        )
                    }
                },
            )
        }
    }

    private data class CategoryExercises(
        val categoryName: String,
        val exerciseNames: List<String>,
    )

    companion object {
        val initialCategories: List<CategoryEntity> = listOf(
            CategoryEntity(name = "腕", displayOrder = 1),
            CategoryEntity(name = "胸", displayOrder = 2),
            CategoryEntity(name = "腹筋", displayOrder = 3),
            CategoryEntity(name = "背中", displayOrder = 4),
            CategoryEntity(name = "下半身", displayOrder = 5),
            CategoryEntity(name = "全身", displayOrder = 6),
        )

        private val initialExercises: List<CategoryExercises> = listOf(
            CategoryExercises("腕", listOf("膝つき腕立て", "ナロープッシュアップ", "椅子ディップス")),
            CategoryExercises("胸", listOf("腕立て伏せ", "ワイドプッシュアップ", "インクラインプッシュアップ")),
            CategoryExercises("腹筋", listOf("クランチ", "レッグレイズ", "バイシクルクランチ", "シットアップ")),
            CategoryExercises("背中", listOf("バックエクステンション", "リバーススノーエンジェル")),
            CategoryExercises("下半身", listOf("スクワット", "ランジ", "カーフレイズ", "ヒップリフト")),
            CategoryExercises("全身", listOf("バーピー", "マウンテンクライマー", "ジャンピングジャック")),
        )
    }
}
