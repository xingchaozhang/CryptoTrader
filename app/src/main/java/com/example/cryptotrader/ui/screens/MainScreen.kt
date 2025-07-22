package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cryptotrader.data.FakeTicker
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.TickerRepository
import com.example.cryptotrader.data.defaultFakeTickers
import kotlinx.coroutines.delay

/**
 * 主容器，包含底部导航栏和不同的顶级页面。
 */
@Composable
fun MainScreen(navController: NavController) {
    val bottomItems = listOf(
        Triple("首页", Icons.Default.Home, "首页"),
        Triple("行情", Icons.Default.ShowChart, "行情"),
        Triple("交易", Icons.Default.SwapHoriz, "交易"),
        Triple("合约", Icons.Default.Description, "合约"),
        Triple("资产", Icons.Default.AccountBalanceWallet, "资产")
    )
    var selectedIndex by remember { mutableStateOf(1) } // 默认进入行情页
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
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedIndex) {
                0 -> PlaceholderScreen("首页")
                1 -> MarketScreen(navController)
                2 -> PlaceholderScreen("交易")
                3 -> PlaceholderScreen("合约")
                4 -> PlaceholderScreen("资产")
            }
        }
    }
}

/**
 * 行情页面，含三层导航：顶部搜索栏 + 一级标签 + 二级标签 + 三级分类。
 * 仅 “市场 → 现货 → 全部” 实现了真实列表，其余均占位。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(navController: NavController) {

    val topTabs        = listOf("自选", "市场", "机会", "观点")
    var selectedTopTab by remember { mutableStateOf(0) }      // 默认进入“自选”
    val favSymbols     by FavoritesRepository.symbols.collectAsState()

    /* editMode 初始取决于收藏是否为空 */
    var editMode by remember { mutableStateOf(favSymbols.isEmpty()) }

    /* 当收藏被清空时再次强制进入编辑 */
    LaunchedEffect(favSymbols) {
        if (favSymbols.isEmpty()) editMode = true
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar()

        /* 一级 Tab */
        TabRow(selectedTabIndex = selectedTopTab) {
            topTabs.forEachIndexed { i, t ->
                Tab(
                    selected = selectedTopTab == i,
                    onClick  = { selectedTopTab = i },
                    text     = { TabText(t, selectedTopTab == i) }
                )
            }
        }

        when (selectedTopTab) {
            /* ───── 自选 Tab ───── */
            0 -> if (editMode) {
                FavoritesEditScreen { chosen ->
                    FavoritesRepository.replaceAll(chosen)
                    editMode = false           // 保存后切回列表
                }
            } else {
                FavoritesListScreen(
                    navController = navController,
                    onEditClick   = { editMode = true }  // 右上✎
                )
            }

            /* ───── 市场 Tab ───── */
            1 -> MarketCategoryTabs(navController)

            else -> PlaceholderScreen(topTabs[selectedTopTab])
        }
    }
}

/* Tab 文本提取 */
@Composable private fun TabText(text: String, selected: Boolean) =
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (selected)
            MaterialTheme.colorScheme.onBackground
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )

/**
 * 二级标签：现货 / 合约。仅现货有下一层分类。
 */
@Composable
fun MarketCategoryTabs(navController: NavController) {
    val secondTabs = listOf("现货", "合约")
    var selectedSecondTab by remember { mutableStateOf(0) }
    TabRow(selectedTabIndex = selectedSecondTab) {
        secondTabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedSecondTab == index,
                onClick = { selectedSecondTab = index },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedSecondTab == index)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )
        }
    }
    when (selectedSecondTab) {
        0 -> SpotCategoryTabs(navController)
        1 -> PlaceholderScreen("合约")
    }
}

/**
 * 现货分类横向列表。仅 “全部” 渲染动态数据。
 */
@Composable
fun SpotCategoryTabs(navController: NavController) {
    var editMode by remember { mutableStateOf(false) }
    val categories = listOf("全部", "创新区", "AI", "Meme专区", "Solana", "全部")
    var selectedCategory by remember { mutableStateOf(0) }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedCategory
            val backgroundColor =
                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                else Color.Transparent
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
        0 -> SpotAllList(navController)
        else -> PlaceholderScreen(categories[selectedCategory])
    }
}

/**
 * “现货 → 全部” 的动态列表，每 200ms 刷新一次。
 */
@Composable
fun SpotAllList(navController: NavController) {
    val tickMap by TickerRepository.tickers.collectAsState()
    val tickers = remember(tickMap) { tickMap.values.toList() }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "币种/成交额",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "最新价",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "涨跌幅",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(tickers) { ticker ->
                TickerRow(
                    ticker = ticker,
                    onClick = {
                        val symbol = ticker.symbol.replace("/", "")
                        navController.navigate("detail/$symbol")
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
        }
    }
}

/**
 * 单行显示一条行情信息，含币对名称、成交额、最新价、人民币价和涨跌幅。
 * 涨跌幅正负显示不同颜色。
 */
@Composable
fun TickerRow(ticker: FakeTicker, onClick: () -> Unit) {
    val (bgColor, textColor) = when {
        ticker.changePercent > 0 -> Pair(Color(0xFFEEF7F0), Color(0xFF007E33))
        ticker.changePercent < 0 -> Pair(Color(0xFFFFEEF0), Color(0xFFD32F2F))
        else -> Pair(Color(0xFFEEEEEE), Color(0xFF888888))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ticker.symbol,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formatVolume(ticker.volume),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatPrice(ticker.price),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formatCny(ticker.cnyPrice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Box(
            modifier = Modifier
                .background(color = bgColor, shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatPercent(ticker.changePercent),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

fun formatVolume(value: Double): String =
    String.format("%,.2f万", value)

fun formatPrice(value: Double): String =
    String.format("%.4f", value)

fun formatCny(value: Double): String =
    String.format("¥ %.2f", value)

fun formatPercent(value: Double): String =
    String.format("%+.2f%%", value)

/**
 * 顶部搜索栏，仅作为装饰。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
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
            // 边框颜色
            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            // 容器颜色分别指定聚焦和非聚焦状态
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * 占位页面，用于尚未实现的 Tab。
 */
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
