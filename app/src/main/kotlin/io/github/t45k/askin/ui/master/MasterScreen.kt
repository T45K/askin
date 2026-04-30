package io.github.t45k.askin.ui.master

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

@Composable
fun MasterScreen(
    uiState: MasterUiState,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Long) -> Unit,
    onAddExerciseClick: (Long) -> Unit,
    onEditExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "マスタ管理",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onAddCategoryClick) {
                Text("カテゴリ追加")
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            Text(
                text = "カテゴリと種目を追加しましょう",
                color = Color(0xFF6D4C41),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.categories, key = { it.category.id }) { categoryWithExercises ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = categoryWithExercises.category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onEditCategoryClick(categoryWithExercises.category.id) }) {
                                        Text("編集")
                                    }
                                    Button(onClick = { onAddExerciseClick(categoryWithExercises.category.id) }) {
                                        Text("種目追加")
                                    }
                                }
                            }
                            if (categoryWithExercises.category.description.isNotBlank()) {
                                Text(
                                    text = categoryWithExercises.category.description,
                                    color = Color(0xFF6D4C41),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            if (categoryWithExercises.exercises.isEmpty()) {
                                Text("種目はまだありません", color = Color(0xFF8D6E63))
                            } else {
                                categoryWithExercises.exercises.forEach { exercise ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("・${exercise.name}")
                                            if (exercise.description.isNotBlank()) {
                                                Text(
                                                    text = exercise.description,
                                                    color = Color(0xFF8D6E63),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        }
                                        OutlinedButton(onClick = { onEditExerciseClick(exercise.id) }) {
                                            Text("編集")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
