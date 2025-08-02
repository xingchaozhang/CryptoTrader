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
import androidx.navigation.NavController
import com.example.cryptotrader.data.TickerRepository
import com.example.cryptotrader.ui.detail.Candle
import com.example.cryptotrader.ui.detail.OrderBookEntry
import com.example.cryptotrader.ui.detail.defaultOrderBook
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
import android.graphics.Color as AndroidColor

private enum class CandlePeriod(val minutes: Int) { M15(15), H1(60), H4(240), D1(1440) }

private fun tabToPeriod(idx: Int) =
    when (idx) {
        0 -> CandlePeriod.M15; 1 -> CandlePeriod.H1; 2 -> CandlePeriod.H4; else -> CandlePeriod.D1
    }

private val random = Random(System.currentTimeMillis())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(symbol: String, navController: NavController) {

    val pairSymbol = remember(symbol) {
        if (symbol.contains("/")) symbol.uppercase()
        else {
            val quotes = listOf("USDT", "BUSD", "FDUSD", "USDC")
            val up = symbol.uppercase()
            val quote = quotes.firstOrNull { up.endsWith(it) }
            if (quote != null) up.dropLast(quote.length) + "/" + quote else up
        }
    }

    val priceFlow = remember { TickerRepository.observe(pairSymbol).filterNotNull() }
    val candlesFlow = remember { MutableStateFlow<List<Candle>>(emptyList()) }
    val orderBookFlow = remember { MutableStateFlow(defaultOrderBook()) }

    var periodTab by remember { mutableStateOf(3) }
    var indicatorTab by remember { mutableStateOf(0) }
    var depthTab by remember { mutableStateOf(0) }
    val periods = listOf("15分", "1小时", "4小时", "日线", "更多")
    val indicators = listOf("MA", "EMA", "BOLL", "SAR", "AVL", "VOL", "MACD")

    val incColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val decColor = MaterialTheme.colorScheme.error.toArgb()
    val neuColor = MaterialTheme.colorScheme.outline.toArgb()
    val colorShadow = AndroidColor.DKGRAY
    var followLast by remember { mutableStateOf(true) }

    LaunchedEffect(pairSymbol, periodTab) {
        val period = tabToPeriod(periodTab)
        candlesFlow.value = emptyList()

        val seed = priceFlow.first().price.toFloat()
        candlesFlow.value = genInitialCandles(seed, period)

        priceFlow.collect { tk ->
            candlesFlow.value = candlesFlow.value.updateByTick(
                period, System.currentTimeMillis(), tk.price.toFloat()
            )
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            orderBookFlow.value = orderBookFlow.value.map {
                it.copy(amount = random.nextFloat() * 0.5f)
            }
            delay(500)
        }
    }

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
                    onClick = {
                        val last = candlesFlow.value.lastOrNull()?.close ?: 0f
                        navController.navigate("trade/${pairSymbol.replace("/", "")}")
                    }
                ) { Text("交易") }
            }
        }
    ) { pad ->

        val candleList by candlesFlow.collectAsState()
        val orderBook by orderBookFlow.collectAsState()
        val displayBook = remember(orderBook) {
            if (orderBook.size >= 20) orderBook
            else List(20) { orderBook[it % orderBook.size] }
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {

            item {
                if (candleList.isNotEmpty())
                    PriceHeader(candleList.last())
                else
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(16.dp)) }
            }

            item {
                ScrollableTabRow(periodTab) {
                    periods.forEachIndexed { i, t ->
                        Tab(i == periodTab, { periodTab = i }) {
                            Text(t, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

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
                                        me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
                                    ) {
                                        followLast = false
                                    }

                                    override fun onChartGestureEnd(
                                        me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
                                    ) {
                                        val lastX = data?.entryCount?.toFloat() ?: 0f
                                        if (highestVisibleX >= lastX - 3f) followLast = true
                                    }

                                    override fun onChartLongPressed(e: MotionEvent?) {}
                                    override fun onChartDoubleTapped(e: MotionEvent?) {}
                                    override fun onChartSingleTapped(e: MotionEvent?) {}
                                    override fun onChartFling(
                                        me1: MotionEvent?, me2: MotionEvent?,
                                        velocityX: Float, velocityY: Float
                                    ) {
                                    }

                                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
                                })

                                marker = OHLCMarker(ctx) { candleList }
                            }
                        },
                        update = { chart ->
                            if (candleList.isEmpty()) {
                                chart.clear(); chart.invalidate()
                                return@AndroidView
                            }

                            val entries = candleList.mapIndexed { i, c ->
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


                            val maxHigh = candleList.maxOf { it.high }
                            val minLow = candleList.minOf { it.low }
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

            item {
                ScrollableTabRow(indicatorTab) {
                    indicators.forEachIndexed { i, s ->
                        Tab(i == indicatorTab, { indicatorTab = i }) {
                            Text(s, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                    Tab(false, {}) { Icon(Icons.Default.Info, null, Modifier.size(16.dp)) }
                }
            }

            item {
                TabRow(depthTab) {
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
                items(displayBook) { OrderBookRow(it) }
            }
        }
    }
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

private fun genInitialCandles(
    seed: Float,
    p: CandlePeriod,
    size: Int = 60
): List<Candle> {
    val now = System.currentTimeMillis()
    val step = p.minutes * 60_000L
    val aligned = now / step * step
    var prev = seed

    val amp = seed * 0.02f

    return List(size) { i ->
        val t = aligned - (size - 1 - i) * step
        val op = prev
        val cl = op + random.nextFloat() * amp * 2 - amp
        val hi = max(op, cl) + random.nextFloat() * amp
        val lo = min(op, cl) - random.nextFloat() * amp
        prev = cl
        Candle(t.toFloat(), op, hi, lo, cl)
    }
}

private fun List<Candle>.updateByTick(
    p: CandlePeriod,
    now: Long,
    price: Float,
    maxBars: Int = 60
): List<Candle> {
    if (isEmpty()) return this
    val step = p.minutes * 60_000L
    val last = last()

    return if (now < last.time.toLong() + step) {

        dropLast(1) + last.copy(
            high = max(last.high, price),
            low = min(last.low, price),
            close = price
        )
    } else {

        val aligned = now / step * step
        val open = last.close
        val newCandle = Candle(
            time = aligned.toFloat(),
            open = open,
            high = max(open, price),
            low = min(open, price),
            close = price
        )
        (this + newCandle).takeLast(maxBars)
    }
}

@Composable
private fun PriceHeader(c: Candle) {
    val pct = (c.close - c.open) / c.open * 100f
    val color = if (pct >= 0) Color(0xFF009E73) else Color(0xFFD32F2F)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text(c.close.fmt(), style = MaterialTheme.typography.headlineLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("≈ ¥${(c.close * 7.18f).roundToInt()}", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("%+.2f%%".format(pct), fontSize = 12.sp, color = color)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                listOf(
                    "24h最高 ${(c.high).fmt()}",
                    "24h最低 ${(c.low).fmt()}",
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

private fun Float.fmt(dec: Int = 2) = "%,.${dec}f".format(this)