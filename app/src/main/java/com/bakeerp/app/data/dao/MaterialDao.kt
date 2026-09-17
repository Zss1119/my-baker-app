package com.bakeerp.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bakeerp.app.data.database.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {

    @Query("SELECT * FROM materials ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<MaterialEntity?>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MaterialEntity?

    @Query("SELECT * FROM materials ORDER BY name")
    suspend fun listAll(): List<MaterialEntity>

    @Query("SELECT * FROM materials WHERE currentStock < safetyStock")
    fun observeLowStock(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MaterialEntity): Long

    @Update
    suspend fun update(entity: MaterialEntity)

    @Delete
    suspend fun delete(entity: MaterialEntity)

    /**
     * 按 id 增量扣减库存；调用方需保证库存充足。
     * 用于销售/制作场景的库存扣减。
     */
    @Query("UPDATE materials SET currentStock = currentStock - :delta, updatedAt = :ts WHERE id = :id")
    suspend fun decreaseStock(id: Long, delta: Double, ts: Long = System.currentTimeMillis())

    /**
     * 按 id 增量增加库存；用于采购入库。
     */
    @Query("UPDATE materials SET currentStock = currentStock + :delta, updatedAt = :ts WHERE id = :id")
    suspend fun increaseStock(id: Long, delta: Double, ts: Long = System.currentTimeMillis())
}