package com.bakeerp.app.data.repository

import com.bakeerp.app.data.dao.ProductionRecordDao
import com.bakeerp.app.data.dao.SaleRecordDao
import com.bakeerp.app.data.database.entity.SaleRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 销售仓储。
 *
 * 自用场景默认按销售即收实现：
 * - 不跟踪赊账/欠款。
 * - 仅记录销售明细与累计金额。
 */
class SaleRepository(
    private val saleDao: SaleRecordDao,
    @Suppress("unused") private val productionDao: ProductionRecordDao
) {

    fun observeAll(): Flow<List<SaleRecordEntity>> = saleDao.observeAll()

    suspend fun listInRange(start: Long, end: Long): List<SaleRecordEntity> =
        saleDao.listInRange(start, end)

    suspend fun sumRevenue(start: Long, end: Long): Double = saleDao.sumRevenue(start, end)

    suspend fun saveSale(record: SaleRecordEntity): Long {
        record.totalPrice = record.quantity * record.unitPrice
        return saleDao.upsert(record)
    }
}