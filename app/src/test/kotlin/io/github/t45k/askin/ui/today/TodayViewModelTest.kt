package io.github.t45k.askin.ui.today

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.MainDispatcherRule
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
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
class TodayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: TrainingRecordRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TrainingRecordRepository(database.trainingRecordDao(), fixedClock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun todayStateShowsEmptyStateThenDailyTotals() = runTest {
        val viewModel = TodayViewModel(repository, fixedClock)
        val empty = viewModel.uiState.first { !it.isLoading }
        val exerciseId = insertExercise("スクワット")

        repository.addReps(LocalDate.of(2026, 4, 30), exerciseId, 60)
        val recorded = viewModel.uiState.first { it.totalReps == 60 }

        assertEquals(0, empty.totalReps)
        assertEquals(listOf("スクワット"), recorded.records.map { it.exerciseName })
    }

    private suspend fun insertExercise(name: String): Long {
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "下半身", displayOrder = 1))
        return database.exerciseDao().insert(ExerciseEntity(name = name, categoryId = categoryId, displayOrder = 1))
    }

    private companion object {
        val fixedClock: Clock = Clock.fixed(
            Instant.parse("2026-04-30T10:00:00Z"),
            ZoneId.of("UTC"),
        )
    }
}
