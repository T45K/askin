package io.github.t45k.askin.ui.record

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.MainDispatcherRule
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.usecase.AddTrainingRecordUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
class RecordEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var trainingRecordRepository: TrainingRecordRepository
    private lateinit var viewModel: RecordEditViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        trainingRecordRepository = TrainingRecordRepository(database.trainingRecordDao(), fixedClock)
        viewModel = RecordEditViewModel(
            masterRepository = MasterRepository(
                database = database,
                categoryDao = database.categoryDao(),
                exerciseDao = database.exerciseDao(),
            ),
            addTrainingRecordUseCase = AddTrainingRecordUseCase(trainingRecordRepository),
            initialDate = LocalDate.of(2026, 4, 30),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveAddsDailyRecordAndAggregatesSameExercise() = runTest {
        val exerciseId = insertExercise("腕立て伏せ")
        viewModel.uiState.first { it.exercises.any { exercise -> exercise.id == exerciseId } }

        viewModel.selectExercise(exerciseId)
        viewModel.updateReps("30")
        viewModel.save()
        viewModel.uiState.first { it.isSaved }

        viewModel.consumeSaved()
        viewModel.selectExercise(exerciseId)
        viewModel.updateReps("20")
        viewModel.save()
        viewModel.uiState.first { it.isSaved }

        val summary = trainingRecordRepository.getDailySummary(LocalDate.of(2026, 4, 30))

        assertEquals(50, summary.totalReps)
        assertEquals(50, summary.records.single().reps)
    }

    @Test
    fun saveShowsValidationErrorWhenExerciseOrRepsAreInvalid() = runTest {
        insertExercise("腕立て伏せ")
        viewModel.updateReps("0")

        viewModel.save()
        val missingExercise = viewModel.uiState.first { it.errorMessage != null }

        viewModel.selectExercise(database.exerciseDao().getActiveExercises().single().id)
        viewModel.updateReps("0")
        viewModel.save()
        val invalidReps = viewModel.uiState.first { it.errorMessage == "回数は1以上で入力してください" }

        assertNotNull(missingExercise.errorMessage)
        assertEquals("回数は1以上で入力してください", invalidReps.errorMessage)
    }

    private suspend fun insertExercise(name: String): Long {
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "胸", displayOrder = 1))
        return database.exerciseDao().insert(
            ExerciseEntity(
                name = name,
                categoryId = categoryId,
                displayOrder = 1,
            ),
        )
    }

    private companion object {
        val fixedClock: Clock = Clock.fixed(
            Instant.parse("2026-04-30T10:00:00Z"),
            ZoneId.of("UTC"),
        )
    }
}
