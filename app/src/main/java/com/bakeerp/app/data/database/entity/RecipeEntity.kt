package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 配方主表。
 *
 * 关键字段：
 * - expectedOutput：配方对应的产出数量（按 outputUnit 计量）。
 * - 配方项（原料用量）单独存放在 RecipeItemEntity，通过 recipeId 关联。
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val productName: String,
    val expectedOutput: Double,
    val outputUnit: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)