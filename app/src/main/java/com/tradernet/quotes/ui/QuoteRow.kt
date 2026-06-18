package com.tradernet.quotes.ui

import android.content.res.Configuration
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.PriceFlash
import com.tradernet.quotes.domain.Quotir
import com.tradernet.quotes.ui.theme.NegativeRed
import com.tradernet.quotes.ui.theme.NeutralGray
import com.tradernet.quotes.ui.theme.PositiveGreen
import com.tradernet.quotes.ui.theme.QuoteTheme
import com.tradernet.quotes.ui.theme.TradernetTheme

private val TickerFontSize = 20.sp
private val LogoSize = 20.dp
private const val FlashDurationMs = 800

@Composable
fun QuoteRow(
    quote: Quotir,
    logo: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val colors = QuoteTheme.colors

    val highlight = remember { Animatable(Color.Transparent) }
    var lastPrice by remember { mutableDoubleStateOf(quote.lastPrice) }
    LaunchedEffect(quote.lastPrice) {
        val changed = quote.lastPrice != lastPrice
        lastPrice = quote.lastPrice
        if (!changed || !quote.hasData) return@LaunchedEffect
        val target = when (quote.flash) {
            PriceFlash.UP -> PositiveGreen
            PriceFlash.DOWN -> NegativeRed
            PriceFlash.NONE -> return@LaunchedEffect
        }
        highlight.snapTo(target)
        highlight.animateTo(Color.Transparent, animationSpec = tween(FlashDurationMs))
    }
    val flashing = highlight.value.alpha > 0.4f

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (logo != null) {
                    Image(
                        bitmap = logo,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(LogoSize),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = quote.ticker,
                    color = colors.textPrimary,
                    fontSize = TickerFontSize,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = quote.subtitle,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = quote.priceText,
                color = if (flashing) Color.White else colors.textPrimary,
                fontSize = TickerFontSize,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(highlight.value)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            if (quote.hasData) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quote.marketClosed) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cloud),
                            contentDescription = null,
                            tint = colors.neutral,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        text = quote.pointsText,
                        color = signColor(quote.changePoints),
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = quote.percentText,
                        color = signColor(quote.changePercent),
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun signColor(value: Double): Color = when {
    value > 0.0 -> PositiveGreen
    value < 0.0 -> NegativeRed
    else -> NeutralGray
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF0E1116,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun QuoteRowPreview() {
    TradernetTheme {
        Surface(color = QuoteTheme.colors.background) {
            Column {
                QuoteRow(
                    quote = Quotir(
                        ticker = "VTBR",
                        name = "VTB",
                        exchange = "MCX",
                        lastPrice = 79.45,
                        changePoints = 4.35,
                        changePercent = 5.79,
                        minStep = 0.001,
                        hasData = true,
                    ),
                    logo = null,
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                )
                QuoteRow(
                    quote = Quotir(
                        ticker = "NIO.9866.AS",
                        name = "NIO Inc",
                        exchange = "HKEX",
                        lastPrice = 41.70,
                        changePoints = -0.39,
                        changePercent = -0.92,
                        minStep = 0.01,
                        hasData = true,
                        marketClosed = true,
                    ),
                    logo = null,
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                )
            }
        }
    }
}
