package com.example.cryptotrader.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.ui.DetailViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry

/**
 * Detailed view showing candle chart and current price for a specific symbol.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                text = "Last: ${String.format("%.2f", priceUpdate.last)} Bid: ${
                    String.format("%.2f", priceUpdate.bid)
                } Ask: ${String.format("%.2f", priceUpdate.ask)}"
            )
            Divider()
            CandlestickChartView(
                candles = candles, modifier = Modifier
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
