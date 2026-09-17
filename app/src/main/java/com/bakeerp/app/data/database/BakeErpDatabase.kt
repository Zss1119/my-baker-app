package com.bakeerp.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bakeerp.app.data.database.entity.CustomerEntity
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.ProductionRecordEntity
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import com.bakeerp.app.data.database.entity.RecipeEntity
import com.bakeerp.app.data.database.entity.RecipeItemEntity
import com.bakeerp.app.data.database.entity.SaleRecordEntity
import com.bakeerp.app.data.dao.CustomerDao
import com.bakeerp.app.data.dao.MaterialDao
import com.bakeerp.app.data.dao.ProductionRecordDao
import com.bakeerp.app.data.dao.PurchaseRecordDao
import com.bakeerp.app.data.dao.RecipeDao
import com.bakeerp.app.data.dao.RecipeItemDao
import com.bakeerp.app.data.dao.SaleRecordDao

/**
 * Room 数据库实例。
 *
 * 数据库版本：v1。
 * 升级策略：自用场景采用 fallbackToDestructiveMigration，便于用户换机数据重置；
 * 若需保留历史数据，请在迁移前导出 JSON 备份。
 */
@Database(
    entities = [
        MaterialEntity::class,
        PurchaseRecordEntity::class,
        RecipeEntity::class,
        RecipeItemEntity::class,
        ProductionRecordEntity::class,
        SaleRecordEntity::class,
        CustomerEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BakeErpDatabase : RoomDatabase() {

    abstract fun materialDao(): MaterialDao
    abstract fun purchaseRecordDao(): PurchaseRecordDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeItemDao(): RecipeItemDao
    abstract fun productionRecordDao(): ProductionRecordDao
    abstract fun saleRecordDao(): SaleRecordDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var instance: BakeErpDatabase? = null

        fun getInstance(context: Context): BakeErpDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BakeErpDatabase::class.java,
                    "bake_erp.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}