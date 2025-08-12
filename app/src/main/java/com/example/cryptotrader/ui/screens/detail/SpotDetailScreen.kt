package com.example.cryptotrader.ui.screens.detail

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cryptotrader.ui.detail.Candle
import com.example.cryptotrader.ui.detail.OrderBookEntry
import com.example.cryptotrader.ui.detail.PriceHeaderState
import com.example.cryptotrader.ui.detail.SpotDetailViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.MPPointF
import android.graphics.Color as AndroidColor

/**
 * 	•SpotDetailScreen 不再做任何业务/计算，只做渲染（订阅 vm 的 StateFlow）。
 * 	•周期切换、价格订阅、K 线拼接、盘口扰动等全部在 [#SpotDetailViewModel]。
 * 	•生成/更新算法放在 DetailData.kt，以后接入真实 REST/WS 也只改 VM 即可。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    symbol: String,
    navController: NavController,
    vm: SpotDetailViewModel = viewModel()
) {
    // 生命周期只启动一次
    LaunchedEffect(symbol) { vm.start(symbol) }

    // 订阅 UI 状态
    val pairSymbol by vm.pairSymbolSlash.collectAsState()
    val candles by vm.candles.collectAsState()
    val orderBook by vm.orderBook.collectAsState()
    val header by vm.header.collectAsState()

    // Tab 文案
    val periods = listOf("15分", "1小时", "4小时", "日线", "更多")
    var periodTab by remember { mutableIntStateOf(3) } // 默认日线
    LaunchedEffect(periodTab) { if (periodTab != 4) vm.setPeriodTab(periodTab) }

    var indicatorTab by remember { mutableIntStateOf(0) }
    var depthTab by remember { mutableIntStateOf(0) }

    // 颜色
    val incColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val decColor = MaterialTheme.colorScheme.error.toArgb()
    val neuColor = MaterialTheme.colorScheme.outline.toArgb()
    val colorShadow = AndroidColor.DKGRAY

    // 跟随最后一根
    var followLast by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pairSymbol, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { navController.navigate("trade/${pairSymbol.replace("/", "")}") }
                ) { Text("交易") }
            }
        }
    ) { pad ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // 价格头
            item {
                if (header != null)
                    PriceHeader(header!!)
                else
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp), Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    }
            }

            // 周期 Tab
            item {
                ScrollableTabRow(periodTab) {
                    periods.forEachIndexed { i, t ->
                        Tab(i == periodTab, { periodTab = i }) {
                            Text(t, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            // K 线图
            item {
                if (periodTab != 4) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        factory = { ctx ->
                            CandleStickChart(ctx).apply {
                                description = Description().apply { text = "" }
                                axisRight.isEnabled = false
                                legend.isEnabled = false
                                setTouchEnabled(true)
                                isDragEnabled = true
                                setScaleEnabled(true)
                                setPinchZoom(true)
                                isHighlightPerTapEnabled = true
                                isHighlightPerDragEnabled = true
                                setAutoScaleMinMaxEnabled(true)
                                setOnChartGestureListener(object : OnChartGestureListener {
                                    override fun onChartGestureStart(
                                        me: MotionEvent?, lastGesture: ChartTouchListener.ChartGesture?
                                    ) {
                                        followLast = false
                                    }

                                    override fun onChartGestureEnd(
                                        me: MotionEvent?, lastGesture: ChartTouchListener.ChartGesture?
                                    ) {
                                        val lastX = data?.entryCount?.toFloat() ?: 0f
                                        if (highestVisibleX >= lastX - 3f) followLast = true
                                    }

                                    override fun onChartLongPressed(e: MotionEvent?) {}
                                    override fun onChartDoubleTapped(e: MotionEvent?) {}
                                    override fun onChartSingleTapped(e: MotionEvent?) {}
                                    override fun onChartFling(
                                        me1: MotionEvent?,
                                        me2: MotionEvent?,
                                        vX: Float,
                                        vY: Float
                                    ) {
                                    }

                                    override fun onChartScale(me: MotionEvent?, sX: Float, sY: Float) {}
                                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
                                })
                                marker = OHLCMarker(ctx) { candles }
                            }
                        },
                        update = { chart ->
                            if (candles.isEmpty()) {
                                chart.clear(); chart.invalidate(); return@AndroidView
                            }
                            val entries = candles.mapIndexed { i, c ->
                                CandleEntry(i.toFloat(), c.high, c.low, c.open, c.close)
                            }
                            val ds = CandleDataSet(entries, "").apply {
                                decreasingColor = decColor
                                decreasingPaintStyle = Paint.Style.FILL
                                increasingColor = incColor
                                increasingPaintStyle = Paint.Style.FILL
                                neutralColor = neuColor
                                shadowColor = colorShadow
                                setDrawValues(false)
                            }
                            if (chart.data == null || chart.data.dataSetCount == 0) {
                                chart.data = CandleData(ds)
                            } else {
                                (chart.data.getDataSetByIndex(0) as CandleDataSet).values = entries
                                chart.data.notifyDataChanged()
                                chart.notifyDataSetChanged()
                            }
                            val maxHigh = candles.maxOf { it.high }
                            val minLow = candles.minOf { it.low }
                            val padding = (maxHigh - minLow) * 0.05f
                            chart.axisLeft.axisMaximum = maxHigh + padding
                            chart.axisLeft.axisMinimum = (minLow - padding).coerceAtLeast(0f)
                            chart.setVisibleXRangeMaximum(40f)
                            if (followLast) chart.moveViewToX(entries.size.toFloat())
                            chart.invalidate()
                        }
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp), Alignment.Center
                    ) {
                        Text("页面正在建设中", fontSize = 12.sp)
                    }
                }
            }

            // 指标 Tab（占位）
            item {
                val indicators = listOf("MA", "EMA", "BOLL", "SAR", "AVL", "VOL", "MACD")
                ScrollableTabRow(indicatorTab) {
                    indicators.forEachIndexed { i, s ->
                        Tab(i == indicatorTab, { indicatorTab = i }) {
                            Text(s, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                    Tab(false, {}) { Icon(Icons.Default.Info, null, Modifier.size(16.dp)) }
                }
            }

            // 订单簿 / 成交
            item {
                TabRow(selectedTabIndex = depthTab) {
                    listOf("订单簿", "成交").forEachIndexed { i, t ->
                        Tab(depthTab == i, { depthTab = i }) {
                            Text(t, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                if (depthTab == 1) TradesPlaceholder()
            }

            if (depthTab == 0) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        Arrangement.SpaceBetween
                    ) {
                        Text("买", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("卖", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
                // VM 已提供 orderBook
                items(orderBook) { OrderBookRow(it) }
            }
        }
    }
}

/* ----------------- 仅 UI 相关的小组件 ----------------- */

@Composable
private fun PriceHeader(h: PriceHeaderState) {
    val color = if (h.pct >= 0) Color(0xFF009E73) else Color(0xFFD32F2F)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text(h.close.fmt(), style = MaterialTheme.typography.headlineLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("≈ ¥${h.cny}", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("%+.2f%%".format(h.pct), fontSize = 12.sp, color = color)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                listOf(
                    "24h最高 ${h.high24h.fmt()}",
                    "24h最低 ${h.low24h.fmt()}",
                    "24h成交量 1.16万",
                    "24h成交额 13.6亿"
                ).forEach { Text(it, fontSize = 12.sp, textAlign = TextAlign.End) }
            }
        }
    }
}

@Composable
private fun OrderBookRow(e: OrderBookEntry) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            e.amount.fmt(2),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp
        )
        Text(
            e.bid.fmt(2),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp
        )
        Text(
            e.ask.fmt(2),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
        Text(
            e.amount.fmt(2),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TradesPlaceholder() =
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center
    ) {
        Text("成交列表占位", fontSize = 12.sp)
    }

private class OHLCMarker(
    ctx: Context,
    private val candlesProvider: () -> List<Candle>
) : MarkerView(ctx, android.R.layout.simple_list_item_1) {
    private val tv: TextView = findViewById<TextView>(android.R.id.text1).apply {
        setBackgroundColor(AndroidColor.WHITE)
        setTextColor(AndroidColor.BLACK)
        textSize = 10f
        setPadding(8, 4, 8, 4)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        (e as? CandleEntry)?.x?.toInt()?.let { idx ->
            candlesProvider().getOrNull(idx)?.let { c ->
                tv.text = "O:${c.open.fmt()} H:${c.high.fmt()} L:${c.low.fmt()} C:${c.close.fmt()}"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height - 10f)
}

private fun Float.fmt(dec: Int = 2) = "%,.${dec}f".format(this)