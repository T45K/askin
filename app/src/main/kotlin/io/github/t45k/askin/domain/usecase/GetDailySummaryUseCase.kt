package io.github.t45k.askin.domain.usecase

import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.model.DailyTrainingSummary
import java.time.LocalDate

class GetDailySummaryUseCase(
    private val trainingRecordRepository: TrainingRecordRepository,
) {
    suspend operator fun invoke(date: LocalDate): DailyTrainingSummary = trainingRecordRepository.getDailySummary(date)
}
