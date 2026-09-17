package com.bakeerp.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bakeerp.app.R
import com.bakeerp.app.ui.home.HomeScreen
import com.bakeerp.app.ui.material.MaterialAddPurchaseScreen
import com.bakeerp.app.ui.material.MaterialDetailScreen
import com.bakeerp.app.ui.material.MaterialEditScreen
import com.bakeerp.app.ui.material.MaterialListScreen
import com.bakeerp.app.ui.production.ProductionAddScreen
import com.bakeerp.app.ui.production.ProductionListScreen
import com.bakeerp.app.ui.recipe.RecipeDetailScreen
import com.bakeerp.app.ui.recipe.RecipeEditScreen
import com.bakeerp.app.ui.recipe.RecipeListScreen
import com.bakeerp.app.ui.sale.SaleAddScreen
import com.bakeerp.app.ui.sale.SaleListScreen
import com.bakeerp.app.ui.settings.SettingsScreen

/**
 * 应用导航图：底部 4 Tab + 设置入口 + 各页面详情/编辑子路由。
 */
object Routes {
    const val HOME = "home"
    const val MATERIAL_LIST = "material/list"
    const val MATERIAL_DETAIL = "material/detail/{id}"
    const val MATERIAL_EDIT = "material/edit?id={id}"
    const val MATERIAL_ADD_PURCHASE = "material/purchase/{id}"
    const val RECIPE_LIST = "recipe/list"
    const val RECIPE_DETAIL = "recipe/detail/{id}"
    const val RECIPE_EDIT = "recipe/edit?id={id}"
    const val PRODUCTION_LIST = "production/list"
    const val PRODUCTION_ADD = "production/add"
    const val SALE_LIST = "sale/list"
    const val SALE_ADD = "sale/add"
    const val SETTINGS = "settings"

    fun materialDetail(id: Long) = "material/detail/$id"
    fun materialEdit(id: Long?) = if (id == null) "material/edit?id=0" else "material/edit?id=$id"
    fun materialAddPurchase(id: Long) = "material/purchase/$id"
    fun recipeDetail(id: Long) = "recipe/detail/$id"
    fun recipeEdit(id: Long?) = if (id == null) "recipe/edit?id=0" else "recipe/edit?id=$id"
}

private data class TabSpec(val route: String, val labelRes: Int, val icon: ImageVector)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val tabs = listOf(
        TabSpec(Routes.HOME, R.string.tab_home, Icons.Filled.Home),
        TabSpec(Routes.MATERIAL_LIST, R.string.tab_material, Icons.Filled.Inventory2),
        TabSpec(Routes.PRODUCTION_LIST, R.string.tab_production, Icons.Filled.LocalDining),
        TabSpec(Routes.SALE_LIST, R.string.tab_sale, Icons.Filled.AccountBalanceWallet)
    )

    Scaffold(
        bottomBar = {
            val current by navController.currentBackStackEntryAsState()
            val currentRoute = current?.destination?.route
            val isTopLevel = tabs.any { it.route == currentRoute } || currentRoute == Routes.SETTINGS
            if (isTopLevel) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current?.destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) { HomeScreen(navController) }

            composable(Routes.MATERIAL_LIST) {
                MaterialListScreen(
                    onItemClick = { id -> navController.navigate(Routes.materialDetail(id)) },
                    onAdd = { navController.navigate(Routes.materialEdit(null)) }
                )
            }
            composable(Routes.MATERIAL_DETAIL) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                MaterialDetailScreen(
                    materialId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.materialEdit(id)) },
                    onAddPurchase = { navController.navigate(Routes.materialAddPurchase(id)) }
                )
            }
            composable(Routes.MATERIAL_EDIT) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                MaterialEditScreen(
                    materialId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MATERIAL_ADD_PURCHASE) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                MaterialAddPurchaseScreen(
                    materialId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.RECIPE_LIST) {
                RecipeListScreen(
                    onItemClick = { id -> navController.navigate(Routes.recipeDetail(id)) },
                    onAdd = { navController.navigate(Routes.recipeEdit(null)) }
                )
            }
            composable(Routes.RECIPE_DETAIL) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                RecipeDetailScreen(recipeId = id, onBack = { navController.popBackStack() })
            }
            composable(Routes.RECIPE_EDIT) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                RecipeEditScreen(recipeId = id, onBack = { navController.popBackStack() })
            }

            composable(Routes.PRODUCTION_LIST) {
                ProductionListScreen(onAdd = { navController.navigate(Routes.PRODUCTION_ADD) })
            }
            composable(Routes.PRODUCTION_ADD) {
                ProductionAddScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SALE_LIST) {
                SaleListScreen(onAdd = { navController.navigate(Routes.SALE_ADD) })
            }
            composable(Routes.SALE_ADD) {
                SaleAddScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}