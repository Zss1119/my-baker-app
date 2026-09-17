package com.bakeerp.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakeerp.app.data.database.entity.RecipeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeItemDao {

    @Query("SELECT * FROM recipe_items WHERE recipeId = :recipeId")
    fun observeByRecipe(recipeId: Long): Flow<List<RecipeItemEntity>>

    @Query("SELECT * FROM recipe_items WHERE recipeId = :recipeId")
    suspend fun listByRecipe(recipeId: Long): List<RecipeItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RecipeItemEntity>): List<Long>

    @Query("DELETE FROM recipe_items WHERE recipeId = :recipeId")
    suspend fun deleteByRecipe(recipeId: Long)
}