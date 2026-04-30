package io.github.t45k.askin.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.model.DailyTrainingRecord
import io.github.t45k.askin.domain.model.DailyTotal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class HistoryUiState(
    val dailyTotals: List<DailyTotal> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedTotalReps: Int = 0,
    val selectedRecords: List<DailyTrainingRecord> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val trainingRecordRepository: TrainingRecordRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val selectedSummary = selectedDate.flatMapLatest { date ->
        if (date == null) {
            MutableStateFlow(null)
        } else {
            trainingRecordRepository.observeDailySummary(date).map { it }
        }
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        trainingRecordRepository.observeDailyTotals(),
        selectedDate,
        selectedSummary,
    ) { dailyTotals, selectedDate, selectedSummary ->
        val effectiveSelectedDate = selectedDate ?: dailyTotals.firstOrNull()?.date
        HistoryUiState(
            dailyTotals = dailyTotals,
            selectedDate = effectiveSelectedDate,
            selectedTotalReps = selectedSummary?.totalReps
                ?: dailyTotals.firstOrNull { it.date == effectiveSelectedDate }?.totalReps
                ?: 0,
            selectedRecords = selectedSummary?.records.orEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HistoryUiState(),
    )

    fun selectDate(date: LocalDate) {
        selectedDate.update { date }
    }

    companion object {
        fun factory(database: AppDatabase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(
                    TrainingRecordRepository(database.trainingRecordDao()),
                ) as T
            }
        }
    }
}
