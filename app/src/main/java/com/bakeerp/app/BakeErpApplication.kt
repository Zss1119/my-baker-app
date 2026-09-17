package com.bakeerp.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import com.bakeerp.app.data.database.BakeErpDatabase
import com.bakeerp.app.data.repository.CustomerRepository
import com.bakeerp.app.data.repository.MaterialRepository
import com.bakeerp.app.data.repository.ProductionRepository
import com.bakeerp.app.data.repository.RecipeRepository
import com.bakeerp.app.data.repository.SaleRepository
import com.bakeerp.app.work.NotificationHelper
import com.bakeerp.app.work.WorkScheduler

/**
 * 应用入口。
 *
 * 职责：
 * 1. 初始化 Room 数据库与各 Repository 单例。
 * 2. 注册通知通道（Android 8.0+ 必需）。
 * 3. 启动保质期与库存预警的周期任务（WorkManager）。
 */
class BakeErpApplication : Application(), Configuration.Provider {

    val database: BakeErpDatabase by lazy { BakeErpDatabase.getInstance(this) }

    val materialRepository: MaterialRepository by lazy {
        MaterialRepository(database.materialDao(), database.purchaseRecordDao())
    }
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepository(database.recipeDao(), database.recipeItemDao())
    }
    val productionRepository: ProductionRepository by lazy {
        ProductionRepository(database.productionRecordDao(), database.recipeDao())
    }
    val saleRepository: SaleRepository by lazy {
        SaleRepository(database.saleRecordDao(), database.productionRecordDao())
    }
    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(database.customerDao())
    }

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannels()
        WorkScheduler.scheduleDailyChecks(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun registerNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val expiryChannel = NotificationChannel(
            NotificationHelper.CHANNEL_EXPIRY,
            getString(R.string.notification_channel_expiry_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_expiry_desc)
        }

        val inventoryChannel = NotificationChannel(
            NotificationHelper.CHANNEL_INVENTORY,
            getString(R.string.notification_channel_inventory_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_inventory_desc)
        }

        nm.createNotificationChannels(listOf(expiryChannel, inventoryChannel))
    }
}