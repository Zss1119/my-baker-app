package com.bakeerp.app.data.repository

import com.bakeerp.app.data.dao.RecipeDao
import com.bakeerp.app.data.dao.RecipeItemDao
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.RecipeEntity
import com.bakeerp.app.data.database.entity.RecipeItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 配方仓储。
 *
 * 关键能力：
 * - saveRecipe：保存配方主表 + 替换所有配方项（先清后插）。
 * - observeRecipeWithItems：组合观察配方与其原料用量。
 */
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val recipeItemDao: RecipeItemDao
) {

    fun observeAllRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAll()

    fun observeRecipe(id: Long): Flow<RecipeEntity?> = recipeDao.observeById(id)

    fun observeRecipeItems(recipeId: Long): Flow<List<RecipeItemEntity>> =
        recipeItemDao.observeByRecipe(recipeId)

    fun observeRecipeDetail(
        recipeId: Long,
        materialFlow: Flow<List<MaterialEntity>>
    ): Flow<RecipeDetail> = combine(
        recipeDao.observeById(recipeId),
        recipeItemDao.observeByRecipe(recipeId),
        materialFlow
    ) { recipe, items, materials ->
        RecipeDetail(
            recipe = recipe,
            items = items.map { item ->
                RecipeItemWithMaterial(
                    item = item,
                    material = materials.firstOrNull { it.id == item.materialId }
                )
            }
        )
    }

    suspend fun listAllRecipes(): List<RecipeEntity> = recipeDao.listAll()

    suspend fun findRecipe(id: Long): RecipeEntity? = recipeDao.findById(id)

    /**
     * 保存配方：主表 upsert + 全量替换配方项。
     */
    suspend fun saveRecipe(recipe: RecipeEntity, items: List<RecipeItemEntity>): Long {
        val recipeId = if (recipe.id == 0L) {
            recipeDao.upsert(recipe)
        } else {
            recipeDao.update(recipe.copy(updatedAt = System.currentTimeMillis()))
            recipe.id
        }
        recipeItemDao.deleteByRecipe(recipeId)
        val withRecipeId = items.map { it.copy(recipeId = recipeId) }
        if (withRecipeId.isNotEmpty()) recipeItemDao.upsertAll(withRecipeId)
        return recipeId
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.delete(recipe)
}

/**
 * 配方详情组合视图（含配方主表与各原料用量 + 原料实体）。
 */
data class RecipeDetail(
    val recipe: RecipeEntity?,
    val items: List<RecipeItemWithMaterial>
)

data class RecipeItemWithMaterial(
    val item: RecipeItemEntity,
    val material: MaterialEntity?
)