package com.bakeerp.app.data.repository

import com.bakeerp.app.data.dao.MaterialDao
import com.bakeerp.app.data.dao.PurchaseRecordDao
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 原材料与采购仓储。
 *
 * 业务规则：
 * - 采购入库：新增采购记录 + 增加 materials.currentStock。
 * - 库存扣减：扣减 materials.currentStock；FIFO 扣减 purchase_records.remainingQuantity。
 */
class MaterialRepository(
    private val materialDao: MaterialDao,
    private val purchaseDao: PurchaseRecordDao
) {

    fun observeAllMaterials(): Flow<List<MaterialEntity>> = materialDao.observeAll()
    fun observeMaterial(id: Long): Flow<MaterialEntity?> = materialDao.observeById(id)
    fun observeLowStock(): Flow<List<MaterialEntity>> = materialDao.observeLowStock()
    fun observePurchaseHistory(materialId: Long): Flow<List<PurchaseRecordEntity>> =
        purchaseDao.observeByMaterial(materialId)
    fun observeAllByExpiry(): Flow<List<PurchaseRecordEntity>> = purchaseDao.observeAllByExpiry()

    suspend fun listAllMaterials(): List<MaterialEntity> = materialDao.listAll()

    suspend fun upsertMaterial(entity: MaterialEntity): Long = materialDao.upsert(entity)

    suspend fun deleteMaterial(entity: MaterialEntity) = materialDao.delete(entity)

    /**
     * 采购入库：归一化金额字段后插入采购记录，并累加库存。
     */
    suspend fun addPurchase(record: PurchaseRecordEntity): Long {
        val normalized = record.copy(
            remainingQuantity = record.quantity,
            totalPrice = record.quantity * record.unitPrice
        )
        val id = purchaseDao.upsert(normalized)
        materialDao.increaseStock(normalized.materialId, normalized.quantity)
        return id
    }

    /**
     * 库存扣减：扣减 material.currentStock。
     *
     * 当前按原料总量扣减，不做批次级 FIFO（自用场景成本核算足够）。
     * 如需严格 FIFO，可在 purchase_records 中按到期日顺序逐批扣减 remainingQuantity。
     */
    suspend fun consumeMaterial(materialId: Long, quantity: Double) {
        materialDao.decreaseStock(materialId, quantity)
    }

    /**
     * 查询指定天数内即将过期的批次。
     */
    suspend fun findExpiringBatches(daysAhead: Int): List<PurchaseRecordEntity> {
        val threshold = System.currentTimeMillis() + daysAhead * 24L * 60 * 60 * 1000
        return purchaseDao.findExpiringWithin(threshold)
    }
}