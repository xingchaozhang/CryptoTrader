package com.example.cryptotrader

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * WebSocket 客户端：订阅 Binance Spot 逐笔成交。
 */
class BinancePriceStreamer(symbols: List<String>) : WebSocketListener() {

    private val client = OkHttpClient()
    private val _flow  = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    val tickerFlow: SharedFlow<Pair<String, String>> = _flow

    private val streams = symbols.map { "${it.lowercase()}@trade" }
    private var ws: WebSocket? = null
    private var reconnectJob: Job? = null

    fun start() = connect()
    fun stop()  { ws?.close(1000,null); client.dispatcher.executorService.shutdown() }

    private fun connect() {
        Log.i(TAG, "🔄 connect()")
        ws = client.newWebSocket(
            Request.Builder().url("wss://data-stream.binance.vision/ws").build(),
            this
        )
    }

    override fun onOpen(ws: WebSocket, resp: Response) {
        Log.i(TAG, "✅ onOpen → SUBSCRIBE $streams")
        ws.send(JSONObject().apply {
            put("method", "SUBSCRIBE")
            put("params", JSONArray(streams))
            put("id", 1)
        }.toString())
    }

    override fun onMessage(ws: WebSocket, text: String) {
        val root = JSONObject(text)
        if (root.has("result") || root.has("code")) return                 // ACK / error
        val d = if (root.has("stream")) root.getJSONObject("data") else root
        if (d.has("s") && d.has("p")) {
            val sym = d.getString("s"); val price = d.getString("p")
            _flow.tryEmit(sym to price)
        }
    }

    override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
        Log.e(TAG, "❌ onFailure", t); reconnect()
    }
    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.w(TAG, "⛔ onClosed code=$code reason=$reason"); reconnect()
    }

    private fun reconnect(delayMs: Long = 2000) {
        if (reconnectJob?.isActive == true) return
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "⏳ reconnecting ...")
            delay(delayMs); connect()
        }
    }
    private companion object { const val TAG = "BinancePriceStreamer" }
}
