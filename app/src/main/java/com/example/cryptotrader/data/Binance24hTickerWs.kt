package com.example.cryptotrader.data

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class Ticker24h(
    val symbol: String,          // BTCUSDT）
    val lastPrice: String,       // 最新价 "4297.1900"
    val changePercent: String,   // 24h 涨跌幅（百分比数字字符串，可能负）"3.25"
    val quoteVolume: String,     // 24h 计价币（USDT）成交额 "12345678.9"
    val eventTime: Long
)

@Singleton
class Binance24hTickerWs @Inject constructor(private val client: OkHttpClient) {
    /** 单币种（保留测试页使用） */
    fun streamSpot(symbol: String): Flow<Ticker24h> = callbackFlow {
        val url = "wss://stream.binance.com:9443/ws/${symbol.lowercase(Locale.US)}@ticker"
        val req = Request.Builder().url(url).build()
        val ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                parseFromRaw(text)?.let { trySend(it).isSuccess }
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null); close()
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })
        awaitClose { ws.cancel() }
    }

    /** 多币种组合流（列表用：单连接订多个 @ticker） */
    fun streamSpotCombined(symbols: List<String>): Flow<Ticker24h> = callbackFlow {
        val streams = symbols
            .filter { it.isNotBlank() }
            .joinToString("/") { it.lowercase(Locale.US) + "@ticker" }

        val url = "wss://stream.binance.com:9443/stream?streams=$streams"
        val req = Request.Builder().url(url).build()
        val ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JSONObject(text)
                    val payload = if (root.has("data")) root.getJSONObject("data") else root
                    parseFromDataObj(payload)?.let { trySend(it).isSuccess }
                } catch (_: Throwable) { /* ignore */ }
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null); close()
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })
        awaitClose { ws.cancel() }
    }

    private fun parseFromRaw(text: String): Ticker24h? = try {
        parseFromDataObj(JSONObject(text))
    } catch (_: Throwable) { null }

    private fun parseFromDataObj(o: JSONObject): Ticker24h? = try {
        val s = o.optString("s").uppercase(Locale.US)   // symbol
        val c = o.optString("c")                        // last
        val P = o.optString("P")                        // percent
        val q = o.optString("q")                        // quote volume (USDT)
        val E = o.optLong("E")                          // event time
        if (s.isEmpty() || c.isEmpty() || P.isEmpty()) null
        else Ticker24h(s, c, P, q, E)
    } catch (_: Throwable) { null }
}