package com.bakeerp.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseRecordDao {

    @Query("SELECT * FROM purchase_records WHERE materialId = :materialId ORDER BY purchaseDate DESC")
    fun observeByMaterial(materialId: Long): Flow<List<PurchaseRecordEntity>>

    @Query("SELECT * FROM purchase_records ORDER BY expiryDate ASC")
    fun observeAllByExpiry(): Flow<List<PurchaseRecordEntity>>

    @Query("SELECT * FROM purchase_records WHERE expiryDate IS NOT NULL AND expiryDate <= :threshold AND remainingQuantity > 0 ORDER BY expiryDate ASC")
    suspend fun findExpiringWithin(threshold: Long): List<PurchaseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PurchaseRecordEntity): Long

    @Query("UPDATE purchase_records SET remainingQuantity = remainingQuantity - :delta WHERE id = :id")
    suspend fun consume(id: Long, delta: Double)
}