package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 客户（邻居）档案。
 *
 * 冗余统计字段：
 * - totalOrders：累计成交单数（销售记录创建时累加）。
 * - totalSpent：累计消费金额（销售记录创建时累加）。
 * 便于在销售登记时自动补全历史信息。
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contact: String = "",
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)