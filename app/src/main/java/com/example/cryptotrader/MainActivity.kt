package com.example.cryptotrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cryptotrader.ui.theme.CryptoTraderTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.example.cryptotrader.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoTraderTheme {
                val vm: TradingViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TradingScreen(vm, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TradingScreen(vm: TradingViewModel, modifier: Modifier = Modifier) {
    val entries by vm.entries.collectAsState()
    val price by vm.latestPrice.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Latest Price: $" + String.format("%.2f", price))
        AndroidView(factory = { context ->
            LineChart(context).apply {
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
            }
        }, update = { chart ->
            val dataSet = LineDataSet(entries, "Price").apply {
                setDrawValues(false)
                setDrawCircles(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }, modifier = Modifier.weight(1f))
        Button(onClick = { /* buy action */ }, modifier = Modifier.padding(8.dp)) {
            Text("Buy")
        }
        Button(onClick = { /* sell action */ }, modifier = Modifier.padding(8.dp)) {
            Text("Sell")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TradingPreview() {
    CryptoTraderTheme {
        val vm = TradingViewModel()
        TradingScreen(vm)
    }
}