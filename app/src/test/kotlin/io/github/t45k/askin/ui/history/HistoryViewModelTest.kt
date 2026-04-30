package io.github.t45k.askin.ui.history

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.MainDispatcherRule
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: TrainingRecordRepository
    private lateinit var viewModel: HistoryViewModel

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
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun historyShowsDailyTotalsAndSelectedDateDetails() = runTest {
        val pushUpId = insertExercise("腕立て伏せ", "胸", 1)
        val squatId = insertExercise("スクワット", "下半身", 2)
        val today = LocalDate.of(2026, 4, 30)
        val yesterday = LocalDate.of(2026, 4, 29)

        repository.addReps(today, pushUpId, 30)
        repository.addReps(today, squatId, 60)
        repository.addReps(yesterday, pushUpId, 10)

        val totals = viewModel.uiState.first { it.dailyTotals.size == 2 }
        viewModel.selectDate(today)
        val detail = viewModel.uiState.first { it.selectedDate == today && it.selectedRecords.size == 2 }

        assertEquals(listOf(today, yesterday), totals.dailyTotals.map { it.date })
        assertEquals(90, detail.selectedTotalReps)
        assertEquals(listOf("腕立て伏せ", "スクワット"), detail.selectedRecords.map { it.exerciseName })
    }

    private suspend fun insertExercise(name: String, categoryName: String, displayOrder: Int): Long {
        val categoryId = database.categoryDao().insert(CategoryEntity(name = categoryName, displayOrder = displayOrder))
        return database.exerciseDao().insert(
            ExerciseEntity(
                name = name,
                categoryId = categoryId,
                displayOrder = displayOrder,
            ),
        )
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
