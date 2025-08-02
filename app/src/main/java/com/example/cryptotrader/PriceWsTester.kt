package com.example.cryptotrader

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

/**
 * 运行方式 1：在 onCreate 中直接 new PriceWsTester()
 * 运行方式 2：放在 ViewModel 并在 UI 层观察 priceFlow
 */
class PriceWsTester {

    private val client = OkHttpClient()

    // Flow 方便 UI 观察，也可改成 LiveData
    private val _priceFlow = MutableStateFlow<String?>(null)
    val  priceFlow: StateFlow<String?> = _priceFlow.asStateFlow()

    // ★ Binance 公共推送：最新一笔成交价
    private val url = "wss://stream.binance.com:443/ws/btcusdt@trade"

    private var webSocket: WebSocket? = null

    init {
        connect()
    }

    private fun connect() {
        val req = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(req, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, resp: Response) {
                Log.i("WsTester", "✅ onOpen")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // 解析 JSON，取成交价 p
                val price = JSONObject(text).getString("p")
                _priceFlow.value = price
                Log.i("WsTester", "💹 price = $price")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                Log.e("WsTester", "❌ failure", t)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("WsTester", "⛔ closed: $code $reason")
            }
        })
    }

    fun close() {
        webSocket?.close(1000, null)
    }
}
