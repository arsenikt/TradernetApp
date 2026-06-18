package com.tradernet.quotes.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.ktor.client.call.body
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoRepository(
    private val client: HttpClient = HttpClient(CIO),
) {
    suspend fun logo(ticker: String, sizePx: Int): Bitmap? = withContext(Dispatchers.IO) {
        val bytes = try {
            client.get(LOGO_URL + ticker.lowercase()).body<ByteArray>()
        } catch (_: Throwable) {
            return@withContext null
        }

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
        // Tickers with no logo answer with a 1x1 transparent stub; treat anything
        // degenerate as "no logo" so the row reserves no space for a missing image.
        if (bitmap.width < MIN_LOGO_PX || bitmap.height < MIN_LOGO_PX) {
            bitmap.recycle()
            return@withContext null
        }
        if (bitmap.width == sizePx && bitmap.height == sizePx) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true).also {
                if (it !== bitmap) bitmap.recycle()
            }
        }
    }

    companion object {
        private const val LOGO_URL = "https://tradernet.com/logos/get-logo-by-ticker?ticker="
        // Real logos are 128x128; the "no logo" answer is a 1x1 stub. Anything
        // below this is treated as absent.
        private const val MIN_LOGO_PX = 8
    }
}
