package io.github.t45k.askin.domain.usecase

import io.github.t45k.askin.data.repository.TrainingRecordRepository
import java.time.LocalDate

class AddTrainingRecordUseCase(
    private val trainingRecordRepository: TrainingRecordRepository,
) {
    suspend operator fun invoke(date: LocalDate, exerciseId: Long, reps: Int) {
        trainingRecordRepository.addReps(date, exerciseId, reps)
    }
}
