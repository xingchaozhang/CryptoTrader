package com.example.cryptotrader

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class BinancePriceStreamer(
    private val symbols: List<String> = listOf("btcusdt", "ethusdt")
) : WebSocketListener() {

    private val client = OkHttpClient()
    private val _price = MutableStateFlow<Pair<String, String>?>(null)
    val price: StateFlow<Pair<String, String>?> = _price.asStateFlow()

    private var ws: WebSocket? = null
    private var reconnectJob: Job? = null

    fun start() = connect()
    fun stop()  {
        ws?.close(1000, null)
        client.dispatcher.executorService.shutdown()
    }

    /* ---------------- 连接 ---------------- */

    private fun connect() {
        ws = client.newWebSocket(
            Request.Builder()
                .url("wss://data-stream.binance.vision/ws")      // 动态订阅入口
                .build(),
            this
        )
    }

    /* ---------------- WebSocket 回调 ---------------- */

    override fun onOpen(ws: WebSocket, resp: Response) {
        val streams = symbols.map { "${it.lowercase()}@trade" }
        val subMsg = """{"method":"SUBSCRIBE","params":${JSONArray(streams)},"id":1}"""
        ws.send(subMsg)

        Log.i(TAG, "✅ Connected, SUBSCRIBE $streams")
    }

    override fun onMessage(ws: WebSocket, text: String) {
        val root = JSONObject(text)

        // 1⃣ 回执 / 错误忽略
        if (root.has("result") || root.has("code")) return

        // 2⃣ 解析真正行情
        val tick = if (root.has("stream")) root.getJSONObject("data") else root
        if (tick.has("s") && tick.has("p")) {
            val sym   = tick.getString("s")      // 交易对，如 BTCUSDT
            val price = tick.getString("p")      // 最新成交价

            // 流推送给 UI
            _price.value = sym to price

            // ★★★ 日志输出 ★★★
            Log.i(TAG, "💹 $sym = $price")
        }
    }

    override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
        Log.e(TAG, "❌ failure", t)
        reconnect()
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.w(TAG, "⛔ closed: $code $reason")
        reconnect()
    }

    /* ---------------- 自动重连 ---------------- */

    private fun reconnect(delayMs: Long = 2_000) {
        if (reconnectJob?.isActive == true) return
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delayMs)
            connect()
        }
    }

    private companion object { const val TAG = "PriceStreamer" }
}