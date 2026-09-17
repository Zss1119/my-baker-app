package com.bakeerp.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakeerp.app.data.database.entity.SaleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleRecordDao {

    @Query("SELECT * FROM sale_records ORDER BY saleDate DESC")
    fun observeAll(): Flow<List<SaleRecordEntity>>

    @Query("SELECT * FROM sale_records WHERE saleDate BETWEEN :start AND :end ORDER BY saleDate DESC")
    suspend fun listInRange(start: Long, end: Long): List<SaleRecordEntity>

    @Query("SELECT COALESCE(SUM(totalPrice), 0) FROM sale_records WHERE saleDate BETWEEN :start AND :end")
    suspend fun sumRevenue(start: Long, end: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SaleRecordEntity): Long
}