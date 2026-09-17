package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 原材料主表。
 *
 * 字段说明：
 * - currentStock：当前可用库存（克/毫升/个，按 unit 字段记录）。
 * - safetyStock：安全库存下限，低于此值触发库存预警。
 * - pricePerUnit：单位采购价（人民币），用于成本核算与利润统计。
 */
@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,
    val currentStock: Double = 0.0,
    val safetyStock: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)