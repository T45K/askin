package io.github.t45k.askin.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.repository.MasterRepository
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.model.DailyTrainingRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val totalReps: Int = 0,
    val records: List<DailyTrainingRecord> = emptyList(),
    val isLoading: Boolean = false,
)

class TodayViewModel(
    trainingRecordRepository: TrainingRecordRepository,
    clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<TodayUiState> = trainingRecordRepository
        .observeDailySummary(today)
        .map { summary ->
            TodayUiState(
                date = today,
                totalReps = summary.totalReps,
                records = summary.records,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TodayUiState(date = today, isLoading = true),
        )

    companion object {
        fun factory(database: AppDatabase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val masterRepository = MasterRepository(
                    database = database,
                    categoryDao = database.categoryDao(),
                    exerciseDao = database.exerciseDao(),
                )
                return TodayViewModel(
                    TrainingRecordRepository(database.trainingRecordDao(), masterRepository),
                ) as T
            }
        }
    }
}
