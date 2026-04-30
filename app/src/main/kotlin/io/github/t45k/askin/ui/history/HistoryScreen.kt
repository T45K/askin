package io.github.t45k.askin.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onDateSelected: (LocalDate) -> Unit,
    onAddRecordClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("履歴", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (uiState.dailyTotals.isEmpty()) {
            Text("まだ記録がありません", color = Color(0xFF6D4C41))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.dailyTotals, key = { it.date.toString() }) { total ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDateSelected(total.date) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(total.date.toString())
                            Text("${total.totalReps}回")
                        }
                    }
                }
            }
        }

        uiState.selectedDate?.let { selectedDate ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(selectedDate.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("合計 ${uiState.selectedTotalReps} 回", color = MaterialTheme.colorScheme.primary)
                    uiState.selectedRecords.forEach { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${record.exerciseName}（${record.categoryName}）")
                            Text("${record.reps}回")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onAddRecordClick(selectedDate.toString()) }) {
                            Text("この日に追加")
                        }
                        OutlinedButton(onClick = { onShareClick(selectedDate.toString()) }) {
                            Text("共有")
                        }
                    }
                }
            }
        }
    }
}
