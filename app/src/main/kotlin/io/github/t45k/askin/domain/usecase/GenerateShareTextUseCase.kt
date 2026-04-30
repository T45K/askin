package io.github.t45k.askin.domain.usecase

import io.github.t45k.askin.data.repository.TrainingRecordRepository
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class GenerateShareTextUseCase(
    private val trainingRecordRepository: TrainingRecordRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    suspend operator fun invoke(date: LocalDate): String {
        val summary = trainingRecordRepository.getDailySummary(date)
        val title = if (date == clock.todayIn(timeZone)) {
            "今日の筋トレ記録 💪"
        } else {
            "${date}の筋トレ記録 💪"
        }

        return buildString {
            appendLine(title)
            appendLine("合計 ${summary.totalReps} 回")
            summary.records.forEach { record ->
                appendLine("- ${record.exerciseName} ${record.reps}回")
            }
            append("#筋トレ #自重トレ")
        }
    }
}
