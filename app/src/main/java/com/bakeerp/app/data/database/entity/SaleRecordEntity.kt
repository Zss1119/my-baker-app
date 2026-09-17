package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 销售记录。
 *
 * 关键字段：
 * - productionRecordId：可选关联到具体制作批次，用于追溯与成本还原。
 * - paymentMethod：支付方式（"现金"/"微信"/"支付宝"/"其他"）。
 * - 自用场景默认按销售即收实现，不跟踪赊账。
 */
@Entity(
    tableName = "sale_records",
    foreignKeys = [
        ForeignKey(
            entity = ProductionRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["productionRecordId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("productionRecordId")]
)
data class SaleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productionRecordId: Long? = null,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val customerName: String = "",
    val paymentMethod: String = "现金",
    val saleDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)