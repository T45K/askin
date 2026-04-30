package io.github.t45k.askin.ui.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.domain.model.CategoryWithExercises
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MasterUiState(
    val categories: List<CategoryWithExercises> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

class MasterViewModel(
    private val masterRepository: MasterRepository,
) : ViewModel() {
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MasterUiState> = combine(
        masterRepository.observeActiveCategories(),
        masterRepository.observeActiveExercises(),
        errorMessage,
    ) { categories, exercises, error ->
        MasterUiState(
            categories = categories.map { category ->
                CategoryWithExercises(
                    category = category,
                    exercises = exercises.filter { it.categoryId == category.id },
                )
            },
            errorMessage = error,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MasterUiState(isLoading = true),
    )

    fun addCategory(name: String, description: String, displayOrder: Int) = launchWithError {
        masterRepository.addCategory(name, description, displayOrder)
    }

    fun updateCategory(id: Long, name: String, description: String, displayOrder: Int) = launchWithError {
        masterRepository.updateCategory(id, name, description, displayOrder)
    }

    fun deleteCategory(id: Long) = launchWithError {
        masterRepository.deleteCategory(id)
    }

    fun addExercise(name: String, description: String, categoryId: Long, displayOrder: Int) = launchWithError {
        masterRepository.addExercise(name, description, categoryId, displayOrder)
    }

    fun updateExercise(id: Long, name: String, description: String, categoryId: Long, displayOrder: Int) = launchWithError {
        masterRepository.updateExercise(id, name, description, categoryId, displayOrder)
    }

    fun deleteExercise(id: Long) = launchWithError {
        masterRepository.deleteExercise(id)
    }

    fun clearError() {
        errorMessage.value = null
    }

    private fun launchWithError(block: suspend () -> Unit) {
        viewModelScope.launch {
            errorMessage.value = null
            try {
                block()
            } catch (exception: IllegalArgumentException) {
                errorMessage.value = exception.message
            }
        }
    }

    companion object {
        fun factory(database: AppDatabase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MasterViewModel(
                    MasterRepository(
                        database = database,
                        categoryDao = database.categoryDao(),
                        exerciseDao = database.exerciseDao(),
                    ),
                ) as T
            }
        }
    }
}
