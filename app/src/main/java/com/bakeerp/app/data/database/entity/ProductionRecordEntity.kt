package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 制作记录。
 *
 * 关键字段：
 * - batchCount：批次系数（如 2 表示按 2 倍配方生产）。
 * - producedQuantity：实际产出数量（可能与 batchCount*expectedOutput 不同，留出手工微调空间）。
 * - totalMaterialCost：原料消耗总成本（冗余存储，便于快速展示）。
 * - productionDate：制作时间，作为成品保质期的起点参考。
 */
@Entity(
    tableName = "production_records",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("recipeId")]
)
data class ProductionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val batchCount: Double,
    val producedQuantity: Double,
    val productionDate: Long,
    val totalMaterialCost: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)