package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.FavoritesRepository

/* ---------------- BottomNav 容器 ---------------- */

@Composable
fun MainScreen(navController: NavController) {
    val bottomItems = listOf(
        Triple("首页", Icons.Default.Home, "首页"),
        Triple("行情", Icons.Default.ShowChart, "行情"),
        Triple("交易", Icons.Default.SwapHoriz, "交易"),
        Triple("合约", Icons.Default.Description, "合约"),
        Triple("资产", Icons.Default.AccountBalanceWallet, "资产")
    )
    var selectedIndex by remember { mutableStateOf(1) } // 默认“行情”

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.third) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (selectedIndex) {
                0 -> PlaceholderScreen("首页")
                1 -> MarketScreen(navController)                 // ← 这里面调用 FavoritesListScreen
                2 -> PlaceholderScreen("交易")
                3 -> PlaceholderScreen("合约")
                4 -> PlaceholderScreen("资产")
            }
        }
    }
}

/* ---------------- 行情页：顶部 Tab ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(navController: NavController) {
    val topTabs = listOf("自选", "市场", "机会", "观点")
    var selectedTopTab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        SearchBar()

        TabRow(selectedTabIndex = selectedTopTab) {
            topTabs.forEachIndexed { i, t ->
                Tab(
                    selected = selectedTopTab == i,
                    onClick = { selectedTopTab = i },
                    text = { TabText(t, selectedTopTab == i) }
                )
            }
        }

        when (selectedTopTab) {
            0 -> FavoritesTabHost(
                navController = navController,
            )

            1 -> SpotCategoryTabs(onClickItem = { symbol ->
                navController.navigate("detail/$symbol")
            })

            else -> PlaceholderScreen(topTabs[selectedTopTab])
        }
    }
}

@Composable
fun FavoritesTabHost(
    navController: NavController
) {
    var editing by remember { mutableStateOf(false) }

    if (editing) {
        FavoritesEditScreen(
            onSave = { newSet ->
                FavoritesRepository.replaceAll(newSet)
                editing = false
            },
            onCancel = { editing = false }
        )
    } else {
        FavoritesListScreen(
            navController = navController,
            onEditClick = { editing = true }
        )
    }
}

/* ---------------- 市场 → 现货分类（演示：仅“全部”接入数据） ---------------- */

@Composable
private fun SpotCategoryTabs(
    onClickItem: (String) -> Unit
) {
    val categories = listOf("全部", "创新区", "AI", "Meme专区", "Solana")
    var selectedCategory by remember { mutableStateOf(0) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedCategory
            val backgroundColor = if (isSelected)
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else Color.Transparent
            Surface(
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor
            ) {
                Box(
                    modifier = Modifier
                        .clickable { selectedCategory = index }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    when (selectedCategory) {
        0 -> MarketAllListScreen(onClickItem)
        else -> PlaceholderScreen(categories[selectedCategory])
    }
}

/* ---------------- 市场→全部列表（用同一 VM） ---------------- */

@Composable
private fun MarketAllListScreen(
    onClickItem: (String) -> Unit,
    vm: TickerListViewModel = hiltViewModel()
) {
    val symbols = listOf(
        "ETHUSDT", "BNBUSDT", "SOLUSDT", "SUIUSDT",
        "ARBUSDT", "XLMUSDT", "ONDOUSDT"
    )
    // 进入这个页时切换到“手动模式”
    LaunchedEffect(Unit) { vm.setSymbols(symbols) }

    val rows by vm.rows.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "币种/成交额", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
            Text(
                "最新价", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
            Text(
                "涨跌幅", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(rows, key = { it.symbol }) { row ->
                TickerRow24h(row, onClickItem)
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
            }
        }
    }
}

/* ---------------- 小组件：搜索、Tab 文案、占位 ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar() {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("BTC/USDT") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun TabText(text: String, selected: Boolean) {
    Text(
        text = text,
        style = if (selected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
    )
}

@Composable
fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label 页面正在建设中",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}