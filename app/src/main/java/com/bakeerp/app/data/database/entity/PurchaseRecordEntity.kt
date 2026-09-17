package com.bakeerp.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 采购批次记录。
 *
 * 关键字段：
 * - remainingQuantity：该批次当前剩余可用量（消耗时递减）。
 * - expiryDate：保质期到期时间戳；用于保质期预警。
 * 原材料删除时级联清理采购记录（自用场景下不会大量历史数据）。
 */
@Entity(
    tableName = "purchase_records",
    foreignKeys = [
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("materialId")]
)
data class PurchaseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: Long,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val supplier: String = "",
    val purchaseDate: Long,
    val expiryDate: Long? = null,
    val remainingQuantity: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)