package com.bakeerp.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakeerp.app.data.database.entity.ProductionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionRecordDao {

    @Query("SELECT * FROM production_records ORDER BY productionDate DESC")
    fun observeAll(): Flow<List<ProductionRecordEntity>>

    @Query("SELECT * FROM production_records WHERE productionDate BETWEEN :start AND :end ORDER BY productionDate DESC")
    suspend fun listInRange(start: Long, end: Long): List<ProductionRecordEntity>

    @Query("SELECT * FROM production_records WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ProductionRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProductionRecordEntity): Long
}