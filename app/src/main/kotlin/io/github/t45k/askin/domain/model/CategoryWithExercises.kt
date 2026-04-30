package io.github.t45k.askin.domain.model

data class CategoryWithExercises(
    val category: Category,
    val exercises: List<Exercise>,
)
