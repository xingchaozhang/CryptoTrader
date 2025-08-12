package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.FavoritesRepository
import java.util.Locale

@Composable
fun FavoritesListScreen(
    navController: NavController,
    onEditClick: () -> Unit,
    vm: TickerListViewModel = hiltViewModel()
) {
    // 仅用于判空提示，不再驱动订阅
    val favSymbols by FavoritesRepository.symbols.collectAsState()

    // 进入页面只调用一次，让 VM 跟随收藏
    LaunchedEffect(Unit) { vm.useFavorites() }

    if (favSymbols.isEmpty()) {
        PlaceholderScreen("暂无自选，点击右上 ✎ 添加")
        return
    }

    val rows by vm.rows.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // 表头 + 编辑按钮
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "币种/成交额", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
                Text(
                    "最新价", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f), textAlign = TextAlign.End
                )
                Text(
                    "涨跌幅", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f), textAlign = TextAlign.End
                )
            }
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "编辑自选",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEditClick() }
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(rows, key = { it.symbol }) { row ->
                TickerRow24h(row) { symbol ->
                    navController.navigate("detail/$symbol") // BTCUSDT
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
            }
        }
    }
}

@Composable
fun TickerRow24h(row: TickerRowUi, onClickItem: (String) -> Unit) {
    val upColor = MaterialTheme.colorScheme.primary
    val downColor = MaterialTheme.colorScheme.error
    val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val priceText = remember(row.last) { if (row.last == "--") row.last else row.last.asPrice4() }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClickItem(row.raw?.symbol ?: row.symbol.replace("/", "")) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = row.symbol, style = MaterialTheme.typography.titleMedium)
            Text(
                text = row.volumeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = priceText,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleMedium
        )
        val color = when {
            row.changeText == "--" -> neutral
            row.up -> upColor
            else -> downColor
        }
        Box(Modifier.width(84.dp), contentAlignment = Alignment.CenterEnd) {
            Text(row.changeText, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun String.asPrice4(): String {
    val d = this.replace(",", "").toDoubleOrNull() ?: return this
    return String.format(Locale.US, "%.4f", d)
}