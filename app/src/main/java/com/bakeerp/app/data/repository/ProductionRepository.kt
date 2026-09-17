package com.bakeerp.app.data.repository

import com.bakeerp.app.data.dao.ProductionRecordDao
import com.bakeerp.app.data.dao.RecipeDao
import com.bakeerp.app.data.dao.RecipeItemDao
import com.bakeerp.app.data.database.entity.ProductionRecordEntity
import com.bakeerp.app.data.database.entity.RecipeItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * 制作仓储。
 *
 * 关键能力：
 * - previewConsumption：根据配方与批次量，预览原料消耗清单。
 * - produce：插入制作记录 + 按消耗清单调用 MaterialRepository 扣减库存。
 */
class ProductionRepository(
    private val productionDao: ProductionRecordDao,
    private val recipeDao: RecipeDao
) {

    fun observeAll(): Flow<List<ProductionRecordEntity>> = productionDao.observeAll()

    suspend fun listInRange(start: Long, end: Long): List<ProductionRecordEntity> =
        productionDao.listInRange(start, end)

    /**
     * 预览：按配方 expectedOutput 与批次量，计算单次制作的原料消耗。
     */
    suspend fun previewConsumption(
        recipeId: Long,
        batchCount: Double,
        itemDao: RecipeItemDao
    ): ConsumptionPreview {
        val recipe = recipeDao.findById(recipeId)
            ?: return ConsumptionPreview(recipeName = "", items = emptyList(), totalCost = 0.0)
        val items = itemDao.listByRecipe(recipeId)
        val previewItems = items.map { it.toConsumed(batchCount) }
        val totalCost = previewItems.sumOf { it.subtotalCost }
        return ConsumptionPreview(
            recipeName = recipe.name,
            items = previewItems,
            totalCost = totalCost
        )
    }

    /**
     * 执行制作：落库制作记录（调用方负责后续库存扣减，避免 Repository 循环依赖）。
     */
    suspend fun saveProduction(record: ProductionRecordEntity): Long =
        productionDao.upsert(record)
}

data class ConsumptionPreview(
    val recipeName: String,
    val items: List<ConsumedItem>,
    val totalCost: Double
)

data class ConsumedItem(
    val materialId: Long,
    val materialName: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double,
    val subtotalCost: Double
)

private fun RecipeItemEntity.toConsumed(batchCount: Double): ConsumedItem {
    // 此处 unitPrice 默认 0.0；调用方应在 UI 层关联 MaterialEntity 后重新计算。
    return ConsumedItem(
        materialId = materialId,
        materialName = "",
        unit = "",
        quantity = quantityPerUnit * batchCount,
        unitPrice = 0.0,
        subtotalCost = 0.0
    )
}