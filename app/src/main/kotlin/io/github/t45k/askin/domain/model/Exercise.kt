package io.github.t45k.askin.domain.model

data class Exercise(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val displayOrder: Int,
    val isActive: Boolean,
)
