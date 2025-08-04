package com.example.cryptotrader.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * **实时逐笔成交流**（trade），一个连接同时订阅多个交易对。
 *
 * 服务器每 20s 会 Ping；OkHttp 会自动回复 Pong，因此 **不要** 设置 pingInterval。
 * 24h 强制断线，onClosed 回调里自动重连并重新订阅。
 */
class BinancePriceStreamer(
    symbols: List<String>
) : WebSocketListener() {

    private val client = OkHttpClient()
    private val _tickerFlow = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    /** `(symbol, lastPrice)` 流，UI 层 collectAsState() 即可 */
    val tickerFlow: SharedFlow<Pair<String, String>> = _tickerFlow

    private val streams = symbols.map { "${it.lowercase()}@trade" }
    private var ws: WebSocket? = null
    private var reconnectJob: Job? = null

    fun start() {
        connect()
    }

    fun stop() {
        ws?.close(1000, null)
        client.dispatcher.executorService.shutdown()
    }

    // ---------- WebSocket ----------
    private fun connect() {
        ws = client.newWebSocket(
            Request.Builder().url("wss://data-stream.binance.vision/ws").build(),
            this
        )
    }

    override fun onOpen(ws: WebSocket, resp: Response) {
        ws.send(
            JSONObject().apply {
                put("method", "SUBSCRIBE")
                put("params", JSONArray(streams))
                put("id", 1)
            }.toString()
        )
        Log.i(TAG, "✅ SUBSCRIBE $streams")
    }

    override fun onMessage(ws: WebSocket, text: String) {
        val root = JSONObject(text)
        if (root.has("result") || root.has("code")) return   // 回执 / 错误

        val data = if (root.has("stream")) root.getJSONObject("data") else root
        if (data.has("s") && data.has("p")) {
            _tickerFlow.tryEmit(data.getString("s") to data.getString("p"))
            Log.i(TAG, "💹 ${data.getString("s")} = ${data.getString("p")}")
        }
    }

    override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
        Log.e(TAG, "❌ failure", t)
        reconnect()
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.w(TAG, "⛔ closed $code $reason")
        reconnect()
    }

    private fun reconnect(delayMs: Long = 2_000) {
        if (reconnectJob?.isActive == true) return
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delayMs)
            connect()
        }
    }

    private companion object { const val TAG = "BinancePriceStreamer" }
}

