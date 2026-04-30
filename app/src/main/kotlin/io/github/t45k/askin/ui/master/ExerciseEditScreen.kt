package io.github.t45k.askin.ui.master

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.t45k.askin.domain.model.Category
import io.github.t45k.askin.domain.model.Exercise

@Composable
fun ExerciseEditScreen(
    exercise: Exercise?,
    categories: List<Category>,
    initialCategoryId: Long?,
    onSave: (name: String, categoryId: Long, displayOrder: Int) -> Unit,
    onDeactivate: (() -> Unit)?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstCategoryId = categories.firstOrNull()?.id ?: 0L
    var name by remember(exercise?.id) { mutableStateOf(exercise?.name.orEmpty()) }
    var categoryId by remember(exercise?.id, initialCategoryId) {
        mutableLongStateOf(exercise?.categoryId ?: initialCategoryId ?: firstCategoryId)
    }
    var displayOrderText by remember(exercise?.id) { mutableStateOf((exercise?.displayOrder ?: 1).toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (exercise == null) "種目追加" else "種目編集",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("種目名") },
            singleLine = true,
        )
        Text("カテゴリ", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                FilterChip(
                    selected = category.id == categoryId,
                    onClick = { categoryId = category.id },
                    label = { Text(category.name) },
                )
            }
        }
        OutlinedTextField(
            value = displayOrderText,
            onValueChange = { displayOrderText = it.filter(Char::isDigit) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("表示順") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = categoryId > 0,
                onClick = {
                    onSave(name, categoryId, displayOrderText.toIntOrNull() ?: 1)
                },
            ) {
                Text("保存")
            }
            OutlinedButton(onClick = onBack) {
                Text("戻る")
            }
            if (onDeactivate != null) {
                OutlinedButton(onClick = onDeactivate) {
                    Text("非表示")
                }
            }
        }
    }
}
