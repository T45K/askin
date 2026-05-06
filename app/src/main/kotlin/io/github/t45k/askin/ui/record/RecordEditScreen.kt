package io.github.t45k.askin.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RecordEditScreen(
    uiState: RecordEditUiState,
    onDateChange: (String) -> Unit,
    onExerciseSelected: (Long) -> Unit,
    onRepsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("筋トレを記録", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = uiState.dateText,
            onValueChange = onDateChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("日付") },
            singleLine = true,
        )
        Text("種目", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.exercises.forEach { exercise ->
                FilterChip(
                    selected = exercise.id == uiState.selectedExerciseId,
                    onClick = { onExerciseSelected(exercise.id) },
                    label = { Text(exercise.name) },
                )
            }
        }
        OutlinedTextField(
            value = uiState.repsText,
            onValueChange = onRepsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("回数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (uiState.isSaved) {
            Text("保存しました", color = MaterialTheme.colorScheme.tertiary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSaveClick) {
                Text("保存")
            }
            OutlinedButton(onClick = onBack) {
                Text("戻る")
            }
        }
    }
}
