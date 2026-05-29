package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketStock
import kotlin.random.Random

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

@Composable
fun StockChart(
    stock: MarketStock,
    modifier: Modifier = Modifier
) {
    // Generate a deterministically pseudo-random candlestick list based on the stock's pivot values
    val candles = remember(stock.symbol) {
        val list = mutableListOf<Candle>()
        val rand = Random(stock.symbol.hashCode())
        var current = stock.price * 0.94 // start lower

        for (i in 0 until 24) {
            val open = current
            val bodySize = rand.nextDouble(-1.5, 3.5) / 100.0 * stock.price
            val close = open + bodySize
            val high = maxOf(open, close) + rand.nextDouble(0.0, 1.2) / 100.0 * stock.price
            val low = minOf(open, close) - rand.nextDouble(0.0, 1.2) / 100.0 * stock.price
            
            list.add(Candle(open, high, low, close))
            current = close
        }
        list
    }

    // Dynamic state for drawing current candle tick alongside simulation updates
    val liveLastCandle = remember(candles, stock.price) {
        val prevClose = candles.lastOrNull()?.close ?: (stock.price * 0.99)
        Candle(
            open = prevClose,
            high = maxOf(prevClose, stock.price) + (stock.price * 0.003),
            low = minOf(prevClose, stock.price) - (stock.price * 0.003),
            close = stock.price
        )
    }

    val finalCandles = remember(candles, liveLastCandle) {
        candles.dropLast(1) + liveLastCandle
    }

    // Touch feedback state
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        // Dynamic Chart info strip
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IndicatorTag(name = "EMA 20", color = Color(0xFF2196F3))
                IndicatorTag(name = "EMA 50", color = Color(0xFFFF9800))
                IndicatorTag(name = "EMA 200", color = Color(0xFFE91E63))
            }
            Text(
                text = "Pattern: ${stock.breakoutPattern}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(stock.symbol) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val candleWidth = width / finalCandles.size
                            val index = (offset.x / candleWidth).toInt().coerceIn(0, finalCandles.size - 1)
                            selectedCandleIndex = index
                        },
                        onDrag = { change, _ ->
                            val width = size.width
                            val candleWidth = width / finalCandles.size
                            val index = (change.position.x / candleWidth).toInt().coerceIn(0, finalCandles.size - 1)
                            selectedCandleIndex = index
                        },
                        onDragEnd = {
                            selectedCandleIndex = null
                        },
                        onDragCancel = {
                            selectedCandleIndex = null
                        }
                    )
                }
        ) {
            // Draw Chart elements
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val prices = finalCandles.flatMap { listOf(it.high, it.low) } + listOf(stock.stopLoss, stock.price * 1.05)
                val minPrice = prices.minOrNull() ?: (stock.price * 0.9)
                val maxPrice = prices.maxOrNull() ?: (stock.price * 1.1)
                val priceRange = maxPrice - minPrice

                fun gety(price: Double): Float {
                    return (height - ((price - minPrice) / priceRange * height)).toFloat()
                }

                // 1. Draw Support/Resistance & Target levels (Glass overlays)
                // Stop Loss Zone
                val slY = gety(stock.stopLoss)
                drawRect(
                    color = Color(0x1AFF5252),
                    topLeft = Offset(0f, slY),
                    size = Size(width, (height - slY).coerceAtLeast(0f))
                )
                // Target Zone
                val targetY = gety(stock.price * 1.03) // Approximate Target zone
                drawRect(
                    color = Color(0x1B4CAF50),
                    topLeft = Offset(0f, 0f),
                    size = Size(width, targetY)
                )

                // Draw Horizontal guidelines
                val supportY = gety(stock.support)
                val resistanceY = gety(stock.resistance)

                // Support (Dotted Green)
                drawLine(
                    color = Color(0xFF2E7D32),
                    start = Offset(0f, supportY),
                    end = Offset(width, supportY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                // Resistance (Dotted Orange/Red)
                drawLine(
                    color = Color(0xFFD84315),
                    start = Offset(0f, resistanceY),
                    end = Offset(width, resistanceY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // 2. Draw candles
                val candleWidth = width / finalCandles.size
                val spacing = candleWidth * 0.2f

                finalCandles.forEachIndexed { index, candle ->
                    val x = index * candleWidth + spacing / 2
                    val candleYHigh = gety(candle.high)
                    val candleYLow = gety(candle.low)
                    val candleYOpen = gety(candle.open)
                    val candleYClose = gety(candle.close)

                    val itemColor = if (candle.close >= candle.open) Color(0xFF4CAF50) else Color(0xFFFF5252)

                    // Draw Wick
                    drawLine(
                        color = itemColor,
                        start = Offset(x + (candleWidth - spacing) / 2, candleYHigh),
                        end = Offset(x + (candleWidth - spacing) / 2, candleYLow),
                        strokeWidth = 2.5f
                    )

                    // Draw Body
                    drawRect(
                        color = itemColor,
                        topLeft = Offset(x, minOf(candleYOpen, candleYClose)),
                        size = Size(candleWidth - spacing, kotlin.math.abs(candleYOpen - candleYClose).coerceAtLeast(2f))
                    )
                }

                // 3. Draw EMA lines
                val ema20Path = Path()
                val ema50Path = Path()
                val ema200Path = Path()

                finalCandles.forEachIndexed { index, candle ->
                    val x = index * candleWidth + candleWidth / 2
                    // Build mock EMA curves that align with candles and converge to EMAs
                    val deltaRatio = index.toFloat() / finalCandles.size
                    val ema20Val = candle.close * 0.99 + (stock.ema20 - candle.close * 0.99) * deltaRatio
                    val ema50Val = candle.close * 0.98 + (stock.ema50 - candle.close * 0.98) * deltaRatio
                    val ema200Val = candle.close * 0.95 + (stock.ema200 - candle.close * 0.95) * deltaRatio

                    if (index == 0) {
                        ema20Path.moveTo(x, gety(ema20Val))
                        ema50Path.moveTo(x, gety(ema50Val))
                        ema200Path.moveTo(x, gety(ema200Val))
                    } else {
                        ema20Path.lineTo(x, gety(ema20Val))
                        ema50Path.lineTo(x, gety(ema50Val))
                        ema200Path.lineTo(x, gety(ema200Val))
                    }
                }

                drawPath(ema20Path, color = Color(0xFF2196F3), style = Stroke(width = 3f))
                drawPath(ema50Path, color = Color(0xFFFF9800), style = Stroke(width = 3f))
                drawPath(ema200Path, color = Color(0xFFE91E63), style = Stroke(width = 3f))

                // 4. Overlap technical patterns overlays based on stock type
                when (stock.breakoutPattern) {
                    "Cup and Handle" -> {
                        // Draw Cup curve
                        val cupPath = Path()
                        val startX = width * 0.1f
                        val endX = width * 0.7f
                        val controlY = height * 0.85f
                        cupPath.moveTo(startX, gety(stock.resistance * 0.99))
                        cupPath.quadraticTo(
                            (startX + endX) / 2,
                            controlY,
                            endX,
                            gety(stock.resistance * 0.99)
                        )
                        drawPath(cupPath, color = Color(0xFF9C27B0), style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f,5f), 0f)))

                        // Draw Handle
                        val handlePath = Path()
                        handlePath.moveTo(endX, gety(stock.resistance * 0.99))
                        handlePath.quadraticTo(
                            width * 0.8f,
                            height * 0.7f,
                            width * 0.85f,
                            gety(stock.resistance)
                        )
                        drawPath(handlePath, color = Color(0xFF9C27B0), style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f,5f), 0f)))
                    }
                    "Triangle Breakout" -> {
                        // Resistent trendline descending, Support ascending
                        drawLine(
                            color = Color(0xFF673AB7),
                            start = Offset(width * 0.1f, gety(stock.resistance * 1.02)),
                            end = Offset(width * 0.85f, gety(stock.resistance)),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                        drawLine(
                            color = Color(0xFF673AB7),
                            start = Offset(width * 0.1f, gety(stock.support * 0.97)),
                            end = Offset(width * 0.85f, gety(stock.resistance)),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }
                    "Flag Pattern" -> {
                        // Channel of consolidation
                        val flagXStart = width * 0.4f
                        val flagXEnd = width * 0.8f
                        drawLine(
                            color = Color(0xFF00BCD4),
                            start = Offset(flagXStart, gety(stock.resistance * 0.99)),
                            end = Offset(flagXEnd, gety(stock.resistance * 0.98)),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFF00BCD4),
                            start = Offset(flagXStart, gety(stock.support * 1.01)),
                            end = Offset(flagXEnd, gety(stock.support * 1.00)),
                            strokeWidth = 3f
                        )
                        // Flagpole
                        drawLine(
                            color = Color(0xFF00BCD4),
                            start = Offset(flagXStart, gety(stock.support * 0.94)),
                            end = Offset(flagXStart, gety(stock.resistance * 0.99)),
                            strokeWidth = 4f
                        )
                    }
                    "Rectangle Breakout" -> {
                        // Drawing rectangular bounds
                        val rStart = width * 0.3f
                        val rEnd = width * 0.85f
                        drawRect(
                            color = Color(0x1200BCD4),
                            topLeft = Offset(rStart, gety(stock.resistance)),
                            size = Size(rEnd - rStart, gety(stock.support) - gety(stock.resistance))
                        )
                    }
                }

                // Draw Breakout green arrow icon at the recent breakout point
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 9f,
                    center = Offset(width * 0.85f, resistanceY)
                )

                // 5. Draw touch crosshair if active
                selectedCandleIndex?.let { index ->
                    val x = index * candleWidth + candleWidth / 2
                    val candle = finalCandles[index]
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(0f, gety(candle.close)),
                        end = Offset(width, gety(candle.close)),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }
            }

            // Price Labels drawn natively inside stock view overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xE6FFFFFF), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Zone Bounds", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Targets: ${stock.targets}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                Text("Resistance: Rs. ${stock.resistance}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD84315))
                Text("Support: Rs. ${stock.support}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                Text("Stop Loss: Rs. ${stock.stopLoss}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828))
            }

            // Crosshair Tooltip
            selectedCandleIndex?.let { index ->
                val candle = finalCandles[index]
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color(0xFF212121), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("O: ${String.format("%.1f", candle.open)}", color = Color.White, fontSize = 10.sp)
                        Text("H: ${String.format("%.1f", candle.high)}", color = Color.White, fontSize = 10.sp)
                        Text("L: ${String.format("%.1f", candle.low)}", color = Color.White, fontSize = 10.sp)
                        Text("C: ${String.format("%.1f", candle.close)}", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun IndicatorTag(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(text = name, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
