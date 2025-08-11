package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.TickerRepository

@Composable
fun FavoritesListScreen(
    navController: NavController,
    onEditClick: () -> Unit
) {
    val favSymbols by FavoritesRepository.symbols.collectAsState()
    val tickMap    by TickerRepository.tickers.collectAsState()

    /* 根据自选列表派生可展示数据 */
    val list by remember {
        derivedStateOf { favSymbols.mapNotNull { tickMap[it] } }
    }

    if (favSymbols.isEmpty()) {
        PlaceholderScreen("暂无自选，点击右上 ✎ 添加")
        return
    }

    Column(Modifier.fillMaxSize()) {
        /* 表头 */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            Arrangement.SpaceBetween
        ) {
            listOf("币种/成交额", "最新价", "涨跌幅").forEach {
                Text(it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        /* 列表 */
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(
                items = list,
                key   = { it.symbol }        // 稳定 key，价格变动时正确重组
            ) { tk ->
                TickerRow(tk) {
                    navController.navigate("detail/${tk.symbol.replace("/", "")}")
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
            }
        }
    }
}