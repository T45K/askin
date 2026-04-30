package io.github.t45k.askin.domain.model

data class Category(
    val id: Long,
    val name: String,
    val description: String = "",
    val displayOrder: Int,
    val isActive: Boolean,
)
