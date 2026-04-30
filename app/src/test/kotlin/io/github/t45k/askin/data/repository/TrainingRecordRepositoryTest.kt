package io.github.t45k.askin.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class TrainingRecordRepositoryTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addRepsAddsToExistingDailyExerciseRecord() = runTest {
        val exerciseId = insertExercise(name = "腕立て伏せ", categoryName = "胸")
        val date = LocalDate.of(2026, 4, 30)
        val repository = createRepository()

        repository.addReps(date, exerciseId, 30)
        repository.addReps(date, exerciseId, 20)

        val record = repository.getRecord(date, "胸", "腕立て伏せ")
        val summary = repository.getDailySummary(date)

        assertNotNull(record)
        assertEquals(50, record?.reps)
        assertEquals(50, summary.totalReps)
        assertEquals(listOf("腕立て伏せ"), summary.records.map { it.exerciseName })
    }

    @Test
    fun addRepsKeepsDifferentExercisesSeparate() = runTest {
        val pushUpId = insertExercise(name = "腕立て伏せ", displayOrder = 1)
        val squatId = insertExercise(name = "スクワット", displayOrder = 2)
        val date = LocalDate.of(2026, 4, 30)
        val repository = createRepository()

        repository.addReps(date, pushUpId, 30)
        repository.addReps(date, squatId, 60)

        val summary = repository.getDailySummary(date)

        assertEquals(90, summary.totalReps)
        assertEquals(listOf("腕立て伏せ", "スクワット"), summary.records.map { it.exerciseName })
        assertEquals(listOf(30, 60), summary.records.map { it.reps })
    }

    @Test
    fun addRepsRejectsZeroOrNegativeReps() = runTest {
        val exerciseId = insertExercise(name = "腕立て伏せ")
        val date = LocalDate.of(2026, 4, 30)
        val repository = createRepository()

        try {
            repository.addReps(date, exerciseId, 0)
            fail("Zero reps should be rejected.")
        } catch (_: IllegalArgumentException) {
        }
        try {
            repository.addReps(date, exerciseId, -1)
            fail("Negative reps should be rejected.")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun addRepsKeepsExerciseNameSnapshotAfterMasterDeletion() = runTest {
        val categoryId = insertCategory(name = "胸", displayOrder = 1)
        val exerciseId = insertExercise(categoryId = categoryId, name = "腕立て伏せ", displayOrder = 1)
        val date = LocalDate.of(2026, 4, 30)
        val repository = createRepository()
        val masterRepository = createMasterRepository()

        repository.addReps(date, exerciseId, 30)
        masterRepository.deleteExercise(exerciseId)

        val summary = repository.getDailySummary(date)

        assertEquals(30, summary.totalReps)
        assertEquals(listOf("腕立て伏せ"), summary.records.map { it.exerciseName })
        assertEquals(listOf("胸"), summary.records.map { it.categoryName })
    }

    @Test
    fun addRepsKeepsCategoryNameSnapshotAfterCategoryDeletion() = runTest {
        val categoryId = insertCategory(name = "脚", displayOrder = 1)
        val exerciseId = insertExercise(categoryId = categoryId, name = "スクワット", displayOrder = 1)
        val date = LocalDate.of(2026, 4, 30)
        val repository = createRepository()
        val masterRepository = createMasterRepository()

        repository.addReps(date, exerciseId, 60)
        masterRepository.deleteCategory(categoryId)

        val summary = repository.getDailySummary(date)

        assertEquals(60, summary.totalReps)
        assertEquals(listOf("スクワット"), summary.records.map { it.exerciseName })
        assertEquals(listOf("脚"), summary.records.map { it.categoryName })
        assertEquals(emptyList<ExerciseEntity>(), database.exerciseDao().getAllExercises())
    }

    private suspend fun insertCategory(name: String, displayOrder: Int = 1): Long = database.categoryDao().insert(
        CategoryEntity(
            name = name,
            displayOrder = displayOrder,
        ),
    )

    private suspend fun insertExercise(
        name: String,
        displayOrder: Int = 1,
        categoryName: String = "テストカテゴリ$name",
    ): Long {
        val categoryId = insertCategory(name = categoryName, displayOrder = displayOrder)
        return insertExercise(categoryId = categoryId, name = name, displayOrder = displayOrder)
    }

    private suspend fun insertExercise(categoryId: Long, name: String, displayOrder: Int = 1): Long {
        return database.exerciseDao().insert(
            ExerciseEntity(
                categoryId = categoryId,
                name = name,
                displayOrder = displayOrder,
            ),
        )
    }

    private fun createRepository(): TrainingRecordRepository = TrainingRecordRepository(
        trainingRecordDao = database.trainingRecordDao(),
        masterRepository = createMasterRepository(),
        clock = fixedClock,
    )

    private fun createMasterRepository(): MasterRepository = MasterRepository(
        database = database,
        categoryDao = database.categoryDao(),
        exerciseDao = database.exerciseDao(),
    )

    private companion object {
        val fixedClock: Clock = Clock.fixed(
            Instant.parse("2026-04-30T10:00:00Z"),
            ZoneId.of("UTC"),
        )
    }
}
