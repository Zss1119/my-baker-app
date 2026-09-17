package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 配方项（配方与原料的中间表）。
 *
 * quantityPerUnit：单份配方对应的原料用量（按配方 expectedOutput 计量）。
 * 即：单次按 expectedOutput 制作时消耗的原料量。
 */
@Entity(
    tableName = "recipe_items",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("recipeId"), Index("materialId")]
)
data class RecipeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val materialId: Long,
    val quantityPerUnit: Double
)