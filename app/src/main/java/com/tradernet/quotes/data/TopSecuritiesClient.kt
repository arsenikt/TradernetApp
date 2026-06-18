package com.tradernet.quotes.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

class TopSecuritiesClient(
    private val client: HttpClient = HttpClient(CIO),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun tickers(exchange: String, type: String): List<String> {
        val response = runCatching {
            client.get(buildUrl(exchange, type)).bodyAsText()
        }.getOrNull() ?: return emptyList()

        val root = runCatching { json.parseToJsonElement(response) }.getOrNull() as? JsonObject
            ?: return emptyList()
        val tickers = root["tickers"] as? JsonArray ?: return emptyList()
        return tickers.mapNotNull { it.jsonPrimitive.contentOrNull }
    }

    private fun buildUrl(exchange: String, type: String): String {
        val cmd =
            """{"cmd":"getTopSecurities","params":{"type":"$type","exchange":"$exchange","gainers":0,"limit":$MAX_LIMIT}}"""
        return "$API_URL?q=${URLEncoder.encode(cmd, "UTF-8")}"
    }

    companion object {
        private const val API_URL = "https://tradernet.com/api/"
        private const val MAX_LIMIT = 100
    }
}
