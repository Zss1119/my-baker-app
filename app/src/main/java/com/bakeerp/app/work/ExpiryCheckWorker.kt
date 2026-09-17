package com.bakeerp.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bakeerp.app.BakeErpApplication
import kotlinx.coroutines.flow.first

/**
 * 保质期与库存巡检 Worker。
 *
 * - 7 日内到期的采购批次汇总为一条保质期提醒。
 * - 库存低于安全线的原料汇总为一条库存提醒。
 *
 * 调度策略：每日 09:00 触发一次（WorkScheduler 负责）。
 */
class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as BakeErpApplication

        val expiring = app.materialRepository.findExpiringBatches(daysAhead = 7)
        if (expiring.isNotEmpty()) {
            val title = "保质期预警（${expiring.size} 批次）"
            val content = expiring.take(3).joinToString("；") { "原料#${it.materialId} 剩 ${it.remainingQuantity}" } +
                if (expiring.size > 3) "…" else ""
            NotificationHelper.notifyExpiry(applicationContext, title, content)
        }

        val lowStockList = app.materialRepository.observeLowStock().first()
        if (lowStockList.isNotEmpty()) {
            val title = "库存预警（${lowStockList.size} 项）"
            val content = lowStockList.take(3).joinToString("；") { "${it.name} 剩 ${it.currentStock} ${it.unit}" } +
                if (lowStockList.size > 3) "…" else ""
            NotificationHelper.notifyInventory(applicationContext, title, content)
        }

        return Result.success()
    }
}