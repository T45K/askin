package io.github.t45k.askin.ui.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.t45k.askin.domain.model.Category

@Composable
fun CategoryEditScreen(
    category: Category?,
    onSave: (name: String, description: String, displayOrder: Int) -> Unit,
    onDeactivate: (() -> Unit)?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var description by remember(category?.id) { mutableStateOf(category?.description.orEmpty()) }
    var displayOrderText by remember(category?.id) { mutableStateOf((category?.displayOrder ?: 1).toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (category == null) "カテゴリ追加" else "カテゴリ編集",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("カテゴリ名") },
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("説明") },
            minLines = 3,
        )
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
                onClick = {
                    onSave(name, description, displayOrderText.toIntOrNull() ?: 1)
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
