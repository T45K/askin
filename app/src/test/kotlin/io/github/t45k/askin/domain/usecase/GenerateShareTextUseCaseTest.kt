package io.github.t45k.askin.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class GenerateShareTextUseCaseTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TrainingRecordRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TrainingRecordRepository(
            trainingRecordDao = database.trainingRecordDao(),
            masterRepository = createMasterRepository(),
            clock = fixedClock,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun invokeGeneratesShareTextForDailySummary() = runTest {
        val pushUpId = insertExercise("腕立て伏せ", "胸", 1)
        val squatId = insertExercise("スクワット", "下半身", 2)
        val date = LocalDate.of(2026, 4, 30)

        repository.addReps(date, pushUpId, 30)
        repository.addReps(date, squatId, 60)

        val shareText = GenerateShareTextUseCase(repository, fixedClock)(date)

        assertEquals(
            """
            今日の筋トレ記録 💪
            合計 90 回
            - 腕立て伏せ 30回
            - スクワット 60回
            #筋トレ #自重トレ
            """.trimIndent(),
            shareText,
        )
    }

    @Test
    fun invokeGeneratesShareTextForEmptyDay() = runTest {
        val shareText = GenerateShareTextUseCase(repository, fixedClock)(LocalDate.of(2026, 4, 29))

        assertEquals(
            """
            2026-04-29の筋トレ記録 💪
            合計 0 回
            #筋トレ #自重トレ
            """.trimIndent(),
            shareText,
        )
    }

    private suspend fun insertExercise(name: String, categoryName: String, displayOrder: Int): Long {
        val categoryId = database.categoryDao().insert(CategoryEntity(name = categoryName, displayOrder = displayOrder))
        return database.exerciseDao().insert(ExerciseEntity(name = name, categoryId = categoryId, displayOrder = displayOrder))
    }

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
