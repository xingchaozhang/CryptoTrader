package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.ui.DetailViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Detailed view showing candle chart and current price for a specific symbol.
 */
@Composable
fun DetailScreen(navController: NavController, symbol: String) {
    val viewModel: DetailViewModel = hiltViewModel()
    val candles = viewModel.candles.collectAsState().value
    val priceUpdate = viewModel.priceUpdate.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(symbol) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Last: ${String.format("%.2f", priceUpdate.last)} Bid: ${String.format("%.2f", priceUpdate.bid)} Ask: ${String.format("%.2f", priceUpdate.ask)}"
            )
            Divider()
            CandlestickChartView(candles = candles, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                navController.navigate("order/$symbol")
            }) {
                Text("Place Order")
            }
        }
    }
}

/**
 * Compose wrapper around MPAndroidChart CandleStickChart.
 */
@Composable
fun CandlestickChartView(candles: List<Candle>, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            CandleStickChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setMaxVisibleValueCount(60)
                setPinchZoom(true)
                setDrawGridBackground(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawLabels(false)
                axisLeft.setDrawGridLines(true)
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = candles.mapIndexed { index, candle ->
                CandleEntry(
                    index.toFloat(),
                    candle.high.toFloat(),
                    candle.low.toFloat(),
                    candle.open.toFloat(),
                    candle.close.toFloat()
                )
            }
            val dataSet = CandleDataSet(entries, "Price").apply {
                decreasingColor = android.graphics.Color.RED
                decreasingPaintStyle = android.graphics.Paint.Style.FILL
                increasingColor = android.graphics.Color.GREEN
                increasingPaintStyle = android.graphics.Paint.Style.FILL
                neutralColor = android.graphics.Color.LTGRAY
                shadowColorSameAsCandle = true
            }
            val data = CandleData(dataSet)
            chart.data = data
            chart.invalidate()
        },
        modifier = modifier
    )
}
