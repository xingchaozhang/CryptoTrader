// TradeScreen.kt
package com.example.cryptotrader.ui.screens.trade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.Order
import com.example.cryptotrader.data.Order.Side
import com.example.cryptotrader.data.Order.Status
import com.example.cryptotrader.ui.detail.OrderBookEntry
import com.example.cryptotrader.ui.screens.trade.OrderType.LIMIT
import com.example.cryptotrader.ui.screens.trade.OrderType.MARKET
import java.util.Locale
import kotlin.math.roundToInt

/* ---------- 入口 ---------- */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TradeScreen(
    symbol: String,
    initPrice: Float,
    navController: NavController,
    vm: TradeViewModel = viewModel(
        key = symbol,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(c: Class<T>): T =
                TradeViewModel(symbol, initPrice) as T
        }
    )
) {
    val ui by vm.ui.collectAsState()

    var showConfirm by remember { mutableStateOf(false) }
    var bottomTab by remember { mutableStateOf(0) }
    var showOnlyCurrent by remember { mutableStateOf(true) }

    var stopEnabled by remember { mutableStateOf(false) }
    var takeProfitTxt by remember { mutableStateOf("") }
    var stopLossTxt by remember { mutableStateOf("") }

    val buyColor = Color(0xFF00B4B4)
    val sellColor = Color(0xFFD32F2F)
    val mainColor = if (ui.isBuy) buyColor else sellColor

    val priceF = ui.priceField.parseFloat() ?: 0f
    val qtyF = ui.qtyField.parseFloat() ?: 0f
    val amountTxt =
        if (priceF == 0f || qtyF == 0f) "" else "%.2f".format(Locale.US, priceF * qtyF)

    Scaffold(
        topBar = {    /* 仅固定标题栏 */
            TopAppBar(
                title = { Text(symbol, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {                       // ❸ 固定，永远可见
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // 兼容手势/三键导航
            ) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                    enabled = priceF > 0f && qtyF > 0f
                ) { Text("交易") }
            }
        }
        /* bottomBar 移除，以便按钮随内容一起滚动；若想固定，请恢复 */
    ) { pad ->

        /* 整页（标题栏除外）单一滚轴 */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
        ) {

            /* ───── 顶部表单 + 深度面板 ───── */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                /* 左侧表单（去掉内部 verticalScroll） */
                Column(
                    Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {

                    /* 买/卖切换 */
                    Row(Modifier.fillMaxWidth()) {
                        SegTab("买入", ui.isBuy) { vm.switchSide(true) }
                        SegTab("卖出", !ui.isBuy) { vm.switchSide(false) }
                    }
                    Spacer(Modifier.height(8.dp))

                    /* 订单类型下拉 */
                    var drop by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(drop, { drop = !drop }) {
                        OutlinedTextField(
                            value = if (ui.orderType == LIMIT) "限价单" else "市价单",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("订单类型") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(drop) }
                        )
                        ExposedDropdownMenu(drop, { drop = false }) {
                            DropdownMenuItem(
                                text = { Text("限价单") },
                                onClick = {
                                    vm.setOrderType(LIMIT); drop = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("市价单") },
                                onClick = {
                                    vm.setOrderType(MARKET); drop = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    if (ui.orderType == LIMIT) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = ui.priceField,
                                onValueChange = vm::onPriceChange,
                                label = { Text("价格 (USDT)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Button(onClick = {
                                val bbo = if (ui.isBuy)
                                    ui.orderBook.minByOrNull { it.ask }?.ask
                                else
                                    ui.orderBook.maxByOrNull { it.bid }?.bid
                                bbo?.let { vm.onPriceChange(it.noComma()) }
                            }) { Text("BBO") }
                        }
                    } else {
                        OutlinedTextField(
                            value = ui.latestPrice.noComma(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("市价 (USDT)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ui.qtyField,
                        onValueChange = vm::onQtyChange,
                        label = { Text("数量 (BTC)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Slider(
                        value = ui.sliderPos,
                        onValueChange = vm::onSliderChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountTxt,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("交易额 (USDT)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = stopEnabled, onCheckedChange = { stopEnabled = it })
                        Text("止盈止损", fontSize = 12.sp)
                    }
                    AnimatedVisibility(
                        visible = stopEnabled,
                        enter = expandVertically(tween(250)) + fadeIn(),
                        exit = shrinkVertically(tween(250)) + fadeOut()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = takeProfitTxt,
                                onValueChange = { takeProfitTxt = it },
                                label = { Text("止盈价") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = stopLossTxt,
                                onValueChange = { stopLossTxt = it },
                                label = { Text("止损价") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("可用 ${ui.availableBalance.noComma()} USDT ➕", fontSize = 12.sp)
                }

                DepthPanel(
                    book = ui.orderBook,
                    last = ui.latestPrice,
                    modifier = Modifier.width(150.dp)
                )
            }

            /* ───── Tabs 与列表 ───── */
            Divider()
            Row(Modifier.fillMaxWidth()) {
                listOf("委托", "资产", "跟单", "机器人").forEachIndexed { i, t ->
                    SegTab2(t, i == bottomTab) { bottomTab = i }
                }
            }
            when (bottomTab) {
                0 -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showOnlyCurrent, onCheckedChange = { showOnlyCurrent = it })
                            Text("只看当前", fontSize = 12.sp)
                        }
                        TextButton(onClick = vm::cancelAll) { Text("全部撤销") }
                    }
                    val list = ui.allOrders.filter {
                        it.status == Status.OPEN && (!showOnlyCurrent || it.symbol == symbol)
                    }
                    OrdersList(list, vm::cancelOrder)
                }

                1 -> HistoryList(ui.allOrders.filter { it.status == Status.FILLED })
                else -> PlaceholderSection()
            }
        }

        /* ───── 确认弹窗 ───── */
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.placeOrder(); showConfirm = false
                        }
                    ) { Text("确认") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("取消") }
                },
                title = { Text("确认下单") },
                text = {
                    Column {
                        InfoRow("方向", if (ui.isBuy) "买入" else "卖出")
                        InfoRow("类型", if (ui.orderType == LIMIT) "限价单" else "市价单")
                        InfoRow("价格", ui.priceField)
                        InfoRow("数量", ui.qtyField)
                        InfoRow("金额", amountTxt)
                    }
                }
            )
        }
    }
}

/* ─────────── 深度面板 ─────────── */
/* ─────────── Depth Panel ─────────── */
@Composable
private fun DepthPanel(
    book: List<OrderBookEntry>,
    last: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .width(150.dp)
            .padding(end = 8.dp)
    ) {

        /* 表头 */
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("价格(USDT)", style = MaterialTheme.typography.labelSmall)
            Text("数量(BTC)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
        }

        /* 卖盘 5 档（红，高→低），用普通 Column */
        val sells = book.take(5).sortedByDescending { it.ask }
        sells.forEach { DepthRow(price = it.ask, qty = it.amount, isBuy = false) }

        /* 最新价行 */
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                last.noComma(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text("")      // 数量列留空
        }
        Text(
            "≈ ¥${(last * 7.18f).roundToInt()}",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.End)
        )

        /* 买盘 5 档（绿，低→高），同样用 Column */
        val buys = book.takeLast(5).sortedBy { it.bid }
        buys.forEach { DepthRow(price = it.bid, qty = it.amount, isBuy = true) }

        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = 0.37f, Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("B 37%", fontSize = 12.sp)
            Text("63% S", fontSize = 12.sp)
        }
    }
}

/* 深度单行 */
@Composable
private fun DepthRow(price: Float, qty: Float, isBuy: Boolean) {
    val priceColor = if (isBuy) Color(0xFF00B4B4) else Color(0xFFD32F2F)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(price.noComma(), color = priceColor)
        Text(
            qty.noComma(4),
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.End
        )
    }
}

/* ─────────── Tabs、列表等（逻辑与旧版一致） ─────────── */
@Composable
private fun RowScope.SegTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .background(bg, MaterialTheme.shapes.small),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp)
    }
}


@Composable
private fun RowScope.SegTab2(text: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick, Modifier.weight(1f)) {
        Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun OrdersList(list: List<Order>, onCancel: (Long) -> Unit) {
    if (list.isEmpty()) PlaceholderSection("暂无委托") else
        LazyColumn(Modifier.height(160.dp)) { items(list) { OrderRow(it, onCancel) } }
}

@Composable
private fun HistoryList(list: List<Order>) {
    if (list.isEmpty()) PlaceholderSection("暂无历史成交") else
        LazyColumn(Modifier.height(160.dp)) { items(list) { HistoryRow(it) } }
}

@Composable
private fun OrderRow(o: Order, onCancel: (Long) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text("${if (o.side == Side.BUY) "买" else "卖"} ${o.qty.noComma(4)}", fontSize = 12.sp)
        Text("¥ ${o.price.noComma()}", fontSize = 12.sp)
        TextButton(onClick = { onCancel(o.id) }) { Text("撤单") }
    }
}

@Composable
private fun HistoryRow(o: Order) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        Arrangement.SpaceBetween
    ) {
        Text("${if (o.side == Side.BUY) "买" else "卖"} ${o.qty.noComma(4)}", fontSize = 12.sp)
        Text("¥ ${o.price.noComma()}", fontSize = 12.sp)
        Text("已成交", fontSize = 12.sp)
    }
}

@Composable
private fun PlaceholderSection(text: String = "页面建设中") =
    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp),
        Alignment.Center
    ) { Text(text) }

/* ---------- 工具扩展 ---------- */
private fun String.parseFloat() = replace(",", "").toFloatOrNull()
private fun Float.noComma(dec: Int = 2) = "%.${dec}f".format(Locale.US, this)

@Composable
private fun InfoRow(k: String, v: String) =
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(k); Text(v) }
