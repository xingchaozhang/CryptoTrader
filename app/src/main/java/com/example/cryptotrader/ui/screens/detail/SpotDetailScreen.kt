// SpotDetailScreen.kt  ——  支持缩放/平移/Marker 的版本
package com.example.cryptotrader.ui.screens.detail

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.cryptotrader.ui.detail.Candle
import com.example.cryptotrader.ui.detail.OrderBookEntry
import com.example.cryptotrader.ui.detail.defaultOrderBook
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
import android.graphics.Color as AndroidColor

/* ----------------------- 周期 & 工具 ----------------------- */
private enum class CandlePeriod(val minutes: Int) { M15(15), H1(60), H4(240), D1(1440) }
private fun tabToPeriod(idx: Int) = when (idx) {
    0 -> CandlePeriod.M15; 1 -> CandlePeriod.H1; 2 -> CandlePeriod.H4; else -> CandlePeriod.D1
}
private val random = Random(System.currentTimeMillis())

/* ----------------------- 入口 Composable ------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(symbol: String, navController: NavController) {

    /* ------- 数据流 ------- */
    val candlesFlow   = remember { MutableStateFlow(generateInitialCandles(CandlePeriod.D1)) }
    val orderBookFlow = remember { MutableStateFlow(defaultOrderBook()) }

    /* ------- UI 状态 ------- */
    var periodTab    by remember { mutableStateOf(3) }
    var indicatorTab by remember { mutableStateOf(0) }
    var depthTab     by remember { mutableStateOf(0) }
    val periods    = listOf("15分","1小时","4小时","日线","更多")
    val indicators = listOf("MA","EMA","BOLL","SAR","AVL","VOL","MACD")

    /* ------- 颜色 ------- */
    val colorInc   = MaterialTheme.colorScheme.tertiary.toArgb()
    val colorDec   = MaterialTheme.colorScheme.error.toArgb()
    val colorNeu   = MaterialTheme.colorScheme.outline.toArgb()
    val colorShadow = AndroidColor.DKGRAY

    var followLast by remember { mutableStateOf(true) }

    /* ------------------- K 线生成逻辑 ------------------- */
    LaunchedEffect(periodTab) {
        val period = tabToPeriod(periodTab)
        candlesFlow.value = generateInitialCandles(period)

        val tickGap        = 1_000L
        var now            = candlesFlow.value.last().time.toLong()

        while (isActive) {
            now += tickGap
            candlesFlow.value = candlesFlow.value.updateByPriceTick(
                period      = period,
                nowMillis   = now,
                latestPrice = nextPrice(candlesFlow.value.last().close)
            )
            delay(tickGap)
        }
    }

    /* ---------------- 订单簿随机量刷新 ------------------ */
    LaunchedEffect(Unit) {
        while (isActive) {
            orderBookFlow.value = orderBookFlow.value.map {
                it.copy(amount = random.nextFloat() * 0.5f)
            }
            delay(500)
        }
    }

    /* -------------------------- UI -------------------------- */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$symbol/USDT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                    onClick = {
                        val last = candlesFlow.value.lastOrNull()?.close ?: 0f
                        navController.navigate("trade/$symbol/$last")
                    }
                ) { Text("交易") }
            }
        }
    ) { pad ->

        val candleList by candlesFlow.collectAsState()
        val orderBook  by orderBookFlow.collectAsState()
        val displayBook = remember(orderBook) {
            if (orderBook.size >= 20) orderBook
            else List(20) { orderBook[it % orderBook.size] }
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {

            /* 1️⃣ 价格头部 */
            item { PriceHeader(candleList.last()) }

            /* 2️⃣ 周期 Tab */
            item {
                ScrollableTabRow(selectedTabIndex = periodTab) {
                    periods.forEachIndexed { i, t ->
                        Tab(selected = i == periodTab, onClick = { periodTab = i }) {
                            Text(t, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            /* 3️⃣ K 线图主体 */
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
                                legend.isEnabled    = false

                                /* ── 交互开关 ── */
                                setTouchEnabled(true)
                                isDragEnabled          = true   // 拖拽平移
                                setScaleEnabled(true)          // X+Y 比例尺
                                isScaleXEnabled        = true
                                isScaleYEnabled        = true
                                setPinchZoom(true)             // 两指缩放
                                isHighlightPerTapEnabled  = true
                                isHighlightPerDragEnabled = true

                                setOnChartGestureListener(object : OnChartGestureListener {
                                    override fun onChartGestureStart(
                                        me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
                                    ) { followLast = false }        // 用户开始操作，停止自动移动

                                    override fun onChartGestureEnd(
                                        me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
                                    ) {
                                        // 如果已经回到最右边（可视区域包含最后 2~3 根），恢复自动
                                        val lastX = data?.entryCount?.toFloat() ?: 0f
                                        if (highestVisibleX >= lastX - 3f) followLast = true
                                    }

                                    /* 其余 4 个接口用不到，可留空 */
                                    override fun onChartLongPressed(e: MotionEvent?) {}
                                    override fun onChartDoubleTapped(e: MotionEvent?) {}
                                    override fun onChartSingleTapped(e: MotionEvent?) {}
                                    override fun onChartFling(
                                        me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float
                                    ) {}
                                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
                                })

                                marker = OHLCMarker(ctx) { candleList }
                            }
                        },
                        update = { chart ->
                            /* 数据集 */
                            val entries = candleList.mapIndexed { idx, c ->
                                CandleEntry(idx.toFloat(), c.high, c.low, c.open, c.close)
                            }
                            val ds = CandleDataSet(entries, "").apply {
                                decreasingColor      = colorDec
                                decreasingPaintStyle = Paint.Style.FILL
                                increasingColor      = colorInc
                                increasingPaintStyle = Paint.Style.FILL
                                neutralColor         = colorNeu
                                shadowColor          = colorShadow
                                setDrawValues(false)
                            }
                            chart.data = CandleData(ds)

                            /* 保持在最新位置 */
                            chart.setVisibleXRangeMaximum(40f)      // 可见最多 40 根

                            chart.data = CandleData(ds)

                            chart.setVisibleXRangeMaximum(40f)
                            if (followLast) {
                                chart.moveViewToX(entries.size.toFloat())
                            }

                            chart.invalidate()
                        }
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("页面正在建设中", fontSize = 12.sp) }
                }
            }

            /* 4️⃣ 指标 Tab 占位 */
            item {
                ScrollableTabRow(selectedTabIndex = indicatorTab) {
                    indicators.forEachIndexed { i, s ->
                        Tab(selected = i == indicatorTab, onClick = { indicatorTab = i }) {
                            Text(s, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                    Tab(selected = false, onClick = {}) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            /* 5️⃣ 深度 / 成交 Tab */
            item {
                TabRow(selectedTabIndex = depthTab) {
                    listOf("订单簿", "成交").forEachIndexed { i, t ->
                        Tab(selected = depthTab == i, onClick = { depthTab = i }) {
                            Text(t, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                if (depthTab == 1) TradesPlaceholder()
            }

            /* 6️⃣ 订单簿 */
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
                items(displayBook) { OrderBookRow(it) }
            }
        }
    }
}

/* ---------------------- 自定义 Marker ---------------------- */
private class OHLCMarker(
    ctx: Context,
    /** Lambda 以便数据变动时仍能取到最新列表 */
    private val candlesProvider: () -> List<Candle>
) : MarkerView(ctx, android.R.layout.simple_list_item_1) {

    private val tv: TextView = findViewById<TextView>(android.R.id.text1).apply {
        setBackgroundColor(AndroidColor.WHITE)
        setTextColor(AndroidColor.BLACK)
        textSize = 10f
        setPadding(8, 4, 8, 4)
    }

    /** 更新文本 */
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        (e as? CandleEntry)?.x?.toInt()?.let { idx ->
            candlesProvider().getOrNull(idx)?.let { c ->
                tv.text = "O:${c.open.fmt()} H:${c.high.fmt()} L:${c.low.fmt()} C:${c.close.fmt()}"
            }
        }
        super.refreshContent(e, highlight)
    }

    /** 让 Marker 出现在选中点上方并水平居中 */
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}

/* ----------------------- 数据逻辑 --------------------------- */

private fun generateInitialCandles(period: CandlePeriod, size: Int = 60): List<Candle> {
    val nowMillis = System.currentTimeMillis()
    val interval  = period.minutes * 60_000L
    val aligned   = nowMillis / interval * interval

    val list = mutableListOf<Candle>()
    var lastClose = 30_000f + random.nextFloat() * 200 - 100
    repeat(size) { i ->
        val t = aligned - (size - 1 - i) * interval
        val open  = lastClose
        val close = open + random.nextFloat() * 1000 - 500
        val high  = max(open, close) + random.nextFloat() * 250
        val low   = min(open, close) - random.nextFloat() * 250
        list += Candle(t.toFloat(), open, high, low, close)
        lastClose = close
    }
    return list
}

private fun List<Candle>.updateByPriceTick(
    period: CandlePeriod,
    nowMillis: Long,
    latestPrice: Float,
    maxBars: Int = 60
): List<Candle> {
    if (isEmpty()) return this
    val interval = period.minutes * 60_000L
    val last     = last()
    val lastOpenTs = last.time.toLong()

    return if (nowMillis < lastOpenTs + interval) {
        dropLast(1) + last.copy(
            high  = max(last.high, latestPrice),
            low   = min(last.low , latestPrice),
            close = latestPrice
        )
    } else {
        val aligned = nowMillis / interval * interval
        val newCandle = Candle(
            time  = aligned.toFloat(),
            open  = last.close,
            high  = latestPrice,
            low   = latestPrice,
            close = latestPrice
        )
        (this + newCandle).takeLast(maxBars)
    }
}

private fun nextPrice(prev: Float): Float = prev + random.nextFloat() * 120f - 60f

/* ---------------- 子组件 & 工具 ----------------------------- */

@Composable
private fun PriceHeader(last: Candle) {
    val pct   = (last.close - last.open) / last.open * 100f
    val color = if (pct >= 0) Color(0xFF009E73) else Color(0xFFD32F2F)

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text(last.close.fmt(), style = MaterialTheme.typography.headlineLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("≈ ¥${(last.close * 7.18f).roundToInt()}", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("%+.2f%%".format(pct), fontSize = 12.sp, color = color)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                listOf(
                    "24h最高价 ${(last.high + 140).fmt()}",
                    "24h最低价 ${(last.low  - 140).fmt()}",
                    "24h成交量 1.16万",
                    "24h成交额 13.6亿"
                ).forEach { Text(it, fontSize = 12.sp, textAlign = TextAlign.End) }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssistChip(onClick = {}, label = { Text("公告") })
            Spacer(Modifier.width(8.dp))
            Text("比特币现货 ETF 昨日净流入 3.63 亿美元…", fontSize = 12.sp)
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
        Text(e.amount.fmt(2), color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f), fontSize = 12.sp)
        Text(e.bid.fmt(2), color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f), fontSize = 12.sp)
        Text(e.ask.fmt(2), color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End)
        Text(e.amount.fmt(2), color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End)
    }
}

@Composable
private fun TradesPlaceholder() = Box(
    Modifier
        .fillMaxWidth()
        .height(160.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant),
    contentAlignment = Alignment.Center
) { Text("成交列表占位", fontSize = 12.sp) }

/* ----------- 简易格式化 ----------- */
private fun Float.fmt(dec: Int = 2) = "%,.${dec}f".format(this)
