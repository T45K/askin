package io.github.t45k.askin.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.model.Exercise
import io.github.t45k.askin.domain.usecase.AddTrainingRecordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class RecordEditUiState(
    val dateText: String,
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseId: Long? = null,
    val repsText: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
)

class RecordEditViewModel(
    masterRepository: MasterRepository,
    private val addTrainingRecordUseCase: AddTrainingRecordUseCase,
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
) : ViewModel() {
    private val formState = MutableStateFlow(RecordEditUiState(dateText = initialDate.toString()))

    val uiState: StateFlow<RecordEditUiState> = combine(
        masterRepository.observeActiveExercises(),
        formState,
    ) { exercises, state ->
        state.copy(exercises = exercises)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = formState.value,
    )

    fun updateDate(dateText: String) {
        formState.update { it.copy(dateText = dateText, isSaved = false, errorMessage = null) }
    }

    fun selectExercise(exerciseId: Long) {
        formState.update { it.copy(selectedExerciseId = exerciseId, isSaved = false, errorMessage = null) }
    }

    fun updateReps(repsText: String) {
        formState.update { it.copy(repsText = repsText.filter(Char::isDigit), isSaved = false, errorMessage = null) }
    }

    fun save() {
        viewModelScope.launch {
            val state = formState.value
            val date = runCatching { LocalDate.parse(state.dateText) }.getOrNull()
            val exerciseId = state.selectedExerciseId
            val reps = state.repsText.toIntOrNull()

            when {
                date == null -> formState.update { it.copy(errorMessage = "日付を yyyy-MM-dd 形式で入力してください") }
                exerciseId == null -> formState.update { it.copy(errorMessage = "種目を選択してください") }
                reps == null || reps <= 0 -> formState.update { it.copy(errorMessage = "回数は1以上で入力してください") }
                else -> {
                    addTrainingRecordUseCase(date, exerciseId, reps)
                    formState.update { it.copy(repsText = "", errorMessage = null, isSaved = true) }
                }
            }
        }
    }

    fun consumeSaved() {
        formState.update { it.copy(isSaved = false) }
    }

    companion object {
        fun factory(database: AppDatabase, initialDate: LocalDate): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val masterRepository = MasterRepository(
                    database = database,
                    categoryDao = database.categoryDao(),
                    exerciseDao = database.exerciseDao(),
                )
                val trainingRecordRepository = TrainingRecordRepository(database.trainingRecordDao(), masterRepository)
                return RecordEditViewModel(
                    masterRepository = masterRepository,
                    addTrainingRecordUseCase = AddTrainingRecordUseCase(trainingRecordRepository),
                    initialDate = initialDate,
                ) as T
            }
        }
    }
}
