package com.tradernet.quotes.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class QuotesSocketClient(
    private val url: String = "wss://wss.tradernet.com/",
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val _connected = MutableStateFlow(false)

    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    fun quoteTicks(tickers: List<String>): Flow<QuoteTick> = channelFlow {
        val producer: ProducerScope<QuoteTick> = this
        val subscribeMessage = buildSubscribeMessage(tickers)

        while (isActive) {
            try {
                client.webSocket(urlString = url) {
                    _connected.value = true
                    send(Frame.Text(subscribeMessage)) 
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            decode(frame.readText())?.let { producer.send(it) }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // Network/parse failure — fall through to the reconnect delay.
            } finally {
                _connected.value = false
            }
            if (!isActive) break
            delay(RECONNECT_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    private fun buildSubscribeMessage(tickers: List<String>): String {
        val list = tickers.joinToString(separator = ",") { "\"$it\"" }
        return "[\"quotes\", [$list]]"
    }

    private fun decode(text: String): QuoteTick? {
        val element = runCatching { json.parseToJsonElement(text) }.getOrNull() ?: return null
        if (element !is JsonArray || element.size < 2) return null
        val event = (element[0] as? JsonPrimitive)?.contentOrNull
        if (event != EVENT_QUOTE) return null
        val body = element[1] as? JsonObject ?: return null
        val ticker = body["c"]?.jsonPrimitive?.contentOrNull ?: return null
        return QuoteTick(
            ticker = ticker,
            lastPrice = body["ltp"]?.jsonPrimitive?.doubleOrNull,
            changePoints = body["chg"]?.jsonPrimitive?.doubleOrNull,
            changePercent = body["pcp"]?.jsonPrimitive?.doubleOrNull,
            exchange = body["ltr"]?.jsonPrimitive?.contentOrNull,
            name = body["name"]?.jsonPrimitive?.contentOrNull,
            minStep = body["min_step"]?.jsonPrimitive?.doubleOrNull,
            prevClose = body["pp"]?.jsonPrimitive?.doubleOrNull,
            marketStatus = body["marketStatus"]?.jsonPrimitive?.contentOrNull,
            isSnapshot = body["init"]?.jsonPrimitive?.intOrNull == 1,
        )
    }

    companion object {
        private const val RECONNECT_DELAY_MS = 2_000L
        private const val EVENT_QUOTE = "q"
    }
}
