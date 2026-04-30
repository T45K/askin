package io.github.t45k.askin.ui.master

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.MainDispatcherRule
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.repository.MasterRepository
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

@RunWith(RobolectricTestRunner::class)
class MasterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var viewModel: MasterViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = MasterViewModel(
            MasterRepository(
                database = database,
                categoryDao = database.categoryDao(),
                exerciseDao = database.exerciseDao(),
            ),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addUpdateAndDeactivateCategoryUpdatesState() = runTest {
        viewModel.addCategory(" 腕 ", " 腕まわりの種目 ", 1)
        val added = viewModel.uiState.first { state -> state.categories.any { it.category.name == "腕" } }
        val categoryId = added.categories.first().category.id

        viewModel.updateCategory(categoryId, "腕・肩", "腕と肩の種目", 2)
        val updated = viewModel.uiState.first { state -> state.categories.any { it.category.name == "腕・肩" } }

        viewModel.deactivateCategory(categoryId)
        val deactivated = viewModel.uiState.first { state -> state.categories.none { it.category.id == categoryId } }

        assertEquals("腕と肩の種目", updated.categories.first().category.description)
        assertEquals(2, updated.categories.first().category.displayOrder)
        assertEquals(emptyList<Any>(), deactivated.categories)
    }

    @Test
    fun addUpdateAndDeactivateExerciseUpdatesState() = runTest {
        viewModel.addCategory("胸", "胸のトレーニング", 1)
        val category = viewModel.uiState.first { it.categories.isNotEmpty() }.categories.first().category

        viewModel.addExercise(" 腕立て伏せ ", " 基本のプッシュアップ ", category.id, 1)
        val exerciseAdded = viewModel.uiState.first { state ->
            state.categories.first().exercises.any { it.name == "腕立て伏せ" }
        }
        val exerciseId = exerciseAdded.categories.first().exercises.first().id

        viewModel.updateExercise(exerciseId, "ワイドプッシュアップ", "胸に効かせる", category.id, 2)
        val exerciseUpdated = viewModel.uiState.first { state ->
            state.categories.first().exercises.any { it.name == "ワイドプッシュアップ" }
        }

        viewModel.deactivateExercise(exerciseId)
        val exerciseDeactivated = viewModel.uiState.first { state ->
            state.categories.first().exercises.none { it.id == exerciseId }
        }

        assertEquals("胸に効かせる", exerciseUpdated.categories.first().exercises.first().description)
        assertEquals(2, exerciseUpdated.categories.first().exercises.first().displayOrder)
        assertEquals(emptyList<Any>(), exerciseDeactivated.categories.first().exercises)
    }

    @Test
    fun blankMasterNamesShowValidationError() = runTest {
        viewModel.addCategory(" ", "説明", 1)

        val state = viewModel.uiState.first { it.errorMessage != null }

        assertNotNull(state.errorMessage)
    }
}
