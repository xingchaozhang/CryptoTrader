package com.example.cryptotrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.example.cryptotrader.ui.screens.MarketOverviewScreen
import com.example.cryptotrader.ui.screens.DetailScreen
import com.example.cryptotrader.ui.screens.OrderScreen
import com.example.cryptotrader.ui.screens.OrderHistoryScreen
import com.example.cryptotrader.ui.theme.TradingAppTheme

/**
 * The main entry point of the application. Sets up navigation and hosts the top-level UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradingAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "overview") {
        composable("overview") {
            MarketOverviewScreen(navController)
        }
        composable(
            route = "detail/{symbol}",
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            DetailScreen(navController, symbol)
        }
        composable(
            route = "order/{symbol}",
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            OrderScreen(navController, symbol)
        }
        composable("orders") {
            OrderHistoryScreen(navController)
        }
    }
}
