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
import com.example.cryptotrader.ui.screens.MainScreen
import com.example.cryptotrader.ui.screens.OrderHistoryScreen
import com.example.cryptotrader.ui.screens.OrderScreen
import com.example.cryptotrader.ui.screens.detail.SpotDetailScreen
import com.example.cryptotrader.ui.screens.trade.TradeScreen
import com.example.cryptotrader.ui.theme.TradingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

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

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable(
            route = "detail/{symbol}",
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStack ->
            val symbol = URLDecoder.decode(backStack.arguments?.getString("symbol")!!, "utf-8")
            SpotDetailScreen(symbol, navController)
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

        composable(
            route = "trade/{symbol}",
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType }
            )
        ) { backStack ->
            val sym = backStack.arguments!!.getString("symbol")!!
            TradeScreen(symbol = sym, navController = navController)
        }
    }
}
