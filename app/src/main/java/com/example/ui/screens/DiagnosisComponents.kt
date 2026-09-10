package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AccordionCard
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.StockData

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DiagnosisDetailsPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedStock by viewModel.selectedStockForDetail.collectAsState()
    val isVisible = selectedStock != null

    // Android 15 Predictive Back Gesture Support
    BackHandler(enabled = isVisible) {
        viewModel.closeDetails()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val stock = selectedStock ?: return@AnimatedVisibility
        DiagnosisContent(stock = stock, viewModel = viewModel)
    }
}

data class ChartDisplayInfo(
    val priceText: String,
    val changeText: String,
    val isPositive: Boolean,
    val dateLabel: String?
)

@Composable
fun DiagnosisContent(
    stock: StockData,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val diagnosisState by viewModel.diagnosisState.collectAsState()
    val selectedRange by viewModel.selectedChartRange.collectAsState()
    val chartSource by viewModel.chartDataSource.collectAsState()

    val watchlistStocks by viewModel.watchlistStocks.collectAsState()
    val isFavorite = remember(watchlistStocks, stock.ticker) {
        watchlistStocks.any { it.ticker == stock.ticker }
    }

    val points by viewModel.selectedStockPrices.collectAsState()
    var activeIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(stock.ticker, selectedRange) {
        activeIndex = null
    }

    val df = remember { java.text.DecimalFormat("#,###") }
    val isKoreanStock = remember(stock.ticker) { stock.ticker.all { it.isDigit() } }

    val displayInfo = remember(points, activeIndex, selectedRange, stock) {
        if (points.isEmpty()) {
            ChartDisplayInfo(
                priceText = "${stock.price}원",
                changeText = stock.change,
                isPositive = stock.isPositive,
                dateLabel = "실시간 시세"
            )
        } else {
            val startPrice = points.first()
            val currentIdx = activeIndex
            if (currentIdx != null && currentIdx in points.indices) {
                // Dragging / touching state (Toss-style!)
                val selectedPrice = points[currentIdx]
                val diff = selectedPrice - startPrice
                val pctChange = if (startPrice > 0) (diff / startPrice) * 100f else 0f
                val pctFormatted = String.format("%.2f", pctChange)

                val formattedDiff = if (isKoreanStock) {
                    df.format(diff.toInt()) + "원"
                } else {
                    "$" + String.format("%.2f", diff)
                }

                val zeroChangeStr = if (isKoreanStock) "0원 (0.00%)" else "$0.00 (0.00%)"
                val changeStr = if (diff > 0) {
                    "+$formattedDiff (+$pctFormatted%)"
                } else if (diff < 0) {
                    "$formattedDiff ($pctFormatted%)"
                } else {
                    zeroChangeStr
                }

                val total = points.size
                val daysAgo = total - 1 - currentIdx
                val date = java.time.LocalDate.now().minusDays(daysAgo.toLong())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일")
                val dateStr = "${date.format(formatter)} 기준 (기간 누적 변동)"

                val formattedPrice = if (isKoreanStock) {
                    df.format(selectedPrice.toInt()) + "원"
                } else {
                    "$" + String.format("%.2f", selectedPrice)
                }

                ChartDisplayInfo(
                    priceText = formattedPrice,
                    changeText = changeStr,
                    isPositive = diff >= 0,
                    dateLabel = dateStr
                )
            } else {
                // Default selected period range state
                val endPrice = points.last()
                val diff = endPrice - startPrice
                val pctChange = if (startPrice > 0) (diff / startPrice) * 100f else 0f
                val pctFormatted = String.format("%.2f", pctChange)

                val formattedDiff = if (isKoreanStock) {
                    df.format(diff.toInt()) + "원"
                } else {
                    "$" + String.format("%.2f", diff)
                }

                val zeroChangeStr = if (isKoreanStock) "0원 (0.00%)" else "$0.00 (0.00%)"
                val changeStr = if (diff > 0) {
                    "+$formattedDiff (+$pctFormatted%)"
                } else if (diff < 0) {
                    "$formattedDiff ($pctFormatted%)"
                } else {
                    zeroChangeStr
                }

                val dateStr = when (selectedRange) {
                    "1개월" -> "최근 1개월 성과"
                    "3개월" -> "최근 3개월 성과"
                    "6개월" -> "최근 6개월 성과"
                    "1년" -> "최근 1년 성과"
                    else -> "전체 성과"
                }

                val formattedPrice = if (isKoreanStock) {
                    df.format(endPrice.toInt()) + "원"
                } else {
                    "$" + String.format("%.2f", endPrice)
                }

                ChartDisplayInfo(
                    priceText = formattedPrice,
                    changeText = changeStr,
                    isPositive = diff >= 0,
                    dateLabel = dateStr
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .displayCutoutPadding()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top Bar Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.closeDetails() },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        viewModel.toggleWatchlist(stock)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "${stock.name}(${stock.ticker}) 진단 정보 공유하기", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Stock Title details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${stock.name} (${stock.ticker})",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = displayInfo.priceText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-1).sp
                )

                val changeColor = if (displayInfo.isPositive) Color(0xFFBA1A1A) else Color(0xFF0058bc)
                Text(
                    text = displayInfo.changeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            // Real-time API Connection & Call Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            chartSource.contains("야후") -> Color(0xFFE8F5E9)
                            chartSource.contains("핀허브") -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFF3E0)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                chartSource.contains("성공") -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            }
                        )
                )
                Text(
                    text = chartSource,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        chartSource.contains("야후") -> Color(0xFF2E7D32)
                        chartSource.contains("핀허브") -> Color(0xFF1565C0)
                        else -> Color(0xFFE65100)
                    }
                )
            }

            if (displayInfo.dateLabel != null) {
                Text(
                    text = displayInfo.dateLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // 3. Interactive Chart Timeline Range Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1개월", "3개월", "6개월", "1년", "전체").forEach { range ->
                val isSelected = selectedRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { viewModel.selectChartRange(range) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Interactive Trend Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            InteractiveTrendChart(
                stock = stock,
                range = selectedRange,
                viewModel = viewModel,
                activeIndex = activeIndex,
                onActiveIndexChange = { activeIndex = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. AI Diagnostics Container with states
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "AI 코어 매칭 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = diagnosisState) {
                is MainViewModel.DiagnosisState.Initial,
                is MainViewModel.DiagnosisState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "AI가 시장 트랜드와 기업 성과를 분석하는 중...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MainViewModel.DiagnosisState.Success -> {
                    val analysis = state.analysis
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DiagnosticBox(
                            title = "강점 (Strengths)",
                            content = analysis.strengths,
                            iconSymbol = "✨",
                            themeColor = MaterialTheme.colorScheme.primary
                        )
                        DiagnosticBox(
                            title = "리스크 (Risks)",
                            content = analysis.risks,
                            iconSymbol = "⚠️",
                            themeColor = Color(0xFFBA1A1A)
                        )
                        DiagnosticBox(
                            title = "전략 제안 (Recommendations)",
                            content = analysis.recommendation,
                            iconSymbol = "💡",
                            themeColor = Color(0xFF8A2BB9)
                        )
                    }
                }
                is MainViewModel.DiagnosisState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFDAD6))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "error",
                                tint = Color(0xFFBA1A1A)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "분석 리포트를 불러오지 못했습니다: ${state.message}",
                                fontSize = 13.sp,
                                color = Color(0xFFBA1A1A)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun InteractiveTrendChart(
    stock: StockData,
    range: String,
    viewModel: MainViewModel,
    activeIndex: Int?,
    onActiveIndexChange: (Int?) -> Unit
) {
    val points by viewModel.selectedStockPrices.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()

    var tappedOffset by remember { mutableStateOf<Offset?>(null) }

    val df = remember { java.text.DecimalFormat("#,###") }
    val isKoreanStock = remember(stock.ticker) { stock.ticker.all { it.isDigit() } }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val monthLabels = remember(points, range) {
        val labels = mutableListOf<Pair<Int, String>>()
        if (points.size < 2) return@remember labels
        val total = points.size
        
        val numLabels = 4
        val step = (total - 1) / (numLabels - 1)
        for (i in 0 until numLabels) {
            val idx = (i * step).coerceAtMost(total - 1)
            val daysAgo = if (range == "전체") {
                (total - 1 - idx) * 7
            } else {
                (total - 1 - idx)
            }
            val date = java.time.LocalDate.now().minusDays(daysAgo.toLong())
            val label = if (range == "전체" || range == "1년" || range == "3년" || range == "5년") {
                "${date.year % 100}년 ${date.monthValue}월"
            } else {
                "${date.monthValue}월"
            }
            labels.add(idx to label)
        }
        labels
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isChartLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "실시간 주식 가격 성과 정보 로드 중...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        } else if (points.isEmpty()) {
            Text(
                text = "역사적 가격 정보를 불러올 수 없습니다.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    val isPressed = event.changes.any { it.pressed }
                                    if (isPressed) {
                                        val width = size.width
                                        val step = if (points.size > 1) width / (points.size - 1) else width
                                        val idx = (change.position.x / step)
                                            .toInt()
                                            .coerceIn(0, points.size - 1)
                                        onActiveIndexChange(idx)
                                        tappedOffset = change.position
                                        change.consume()
                                    } else {
                                        onActiveIndexChange(null)
                                        tappedOffset = null
                                    }
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                val minVal = points.minOrNull() ?: 0f
                val maxVal = points.maxOrNull() ?: 100f
                val valRange = if (maxVal == minVal) 1f else (maxVal - minVal)

                // Reserve 24.dp space at the bottom for Year labels
                val bottomLabelSpace = 24.dp.toPx()
                val bottomY = height - bottomLabelSpace

                val labelColor = if (isDark) {
                    android.graphics.Color.parseColor("#909090")
                } else {
                    android.graphics.Color.parseColor("#606060")
                }
                
                val paint = android.graphics.Paint().apply {
                    color = labelColor
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                }

                val pricePaint = android.graphics.Paint().apply {
                    color = labelColor
                    textSize = 8.5.dp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                }

                // 1. Draw grid horizontal lines with values
                val gridLines = 4
                val stepHeight = bottomY / (gridLines + 1)
                
                // Safe heights margin
                val vertMargin = 16.dp.toPx()
                val workableHeight = bottomY - (vertMargin * 2)

                for (i in 0..gridLines) {
                    val y = stepHeight * (i + 1)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    
                    // Draw y-axis price label at the right side of grid line
                    val ratio = (bottomY - y - vertMargin) / workableHeight
                    val priceVal = minVal + ratio * valRange
                    val formattedPrice = if (isKoreanStock) {
                        df.format(priceVal.toInt()) + "원"
                    } else {
                        "$" + String.format("%.2f", priceVal)
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        formattedPrice,
                        width - 8.dp.toPx(),
                        y - 4.dp.toPx(),
                        pricePaint
                    )
                }

                // 2. Draw high quality spline trend line with gradient fill
                val graphPath = Path()
                val fillPath = Path()

                val strokeWidthPx = 2.5.dp.toPx()

                val coordinates = points.mapIndexed { idx, price ->
                    val x = (idx.toFloat() / (points.size - 1)) * width
                    val ratio = (price - minVal) / valRange
                    val y = bottomY - (ratio * workableHeight + vertMargin)
                    Offset(x, y)
                }

                coordinates.forEachIndexed { idx, point ->
                    if (idx == 0) {
                        graphPath.moveTo(point.x, point.y)
                        fillPath.moveTo(point.x, bottomY)
                        fillPath.lineTo(point.x, point.y)
                    } else {
                        val prevPoint = coordinates[idx - 1]
                        val controlX = (prevPoint.x + point.x) / 2
                        graphPath.cubicTo(
                            controlX, prevPoint.y,
                            controlX, point.y,
                            point.x, point.y
                        )
                        fillPath.cubicTo(
                            controlX, prevPoint.y,
                            controlX, point.y,
                            point.x, point.y
                        )
                    }
                }

                fillPath.lineTo(width, bottomY)
                fillPath.lineTo(0f, bottomY)
                fillPath.close()

                val themeColor = if (stock.isPositive) Color(0xFFBA1A1A) else Color(0xFF0058bc)

                // Area shadow gradient fill underneath the curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(themeColor.copy(alpha = 0.15f), Color.Transparent)
                    )
                )

                // High-performance smooth contour stroke
                drawPath(
                    path = graphPath,
                    color = themeColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )

                // 1.5 Draw vertical month transition guidelines & text labels
                monthLabels.forEach { (index, label) ->
                    val x = (index.toFloat() / (points.size - 1)) * width
                    
                    // Vertical dotted divider line
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(x, 0f),
                        end = Offset(x, bottomY),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Month label text at the bottom
                    val adjustedX = when (index) {
                        0 -> x + 24.dp.toPx()
                        points.size - 1 -> x - 24.dp.toPx()
                        else -> x
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        adjustedX,
                        height - 6.dp.toPx(),
                        paint
                    )
                }

                // 3. Draw active coordinate details (Vertical crosshair cursor)
                val activeIdx = activeIndex
                if (activeIdx != null && activeIdx in coordinates.indices) {
                    val tappedPoint = coordinates[activeIdx]

                    drawLine(
                        color = themeColor.copy(alpha = 0.5f),
                        start = Offset(tappedPoint.x, 0f),
                        end = Offset(tappedPoint.x, bottomY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = tappedPoint
                    )
                    drawCircle(
                        color = themeColor,
                        radius = 4.dp.toPx(),
                        center = tappedPoint
                    )
                }
            }

            // Render interactive floating Tooltip overlay above tapped points
            val activeIdx = activeIndex
            if (activeIdx != null && activeIdx in points.indices) {
                val priceTapped = points[activeIdx]
                val dateLabel = remember(activeIdx, points.size, range) {
                    val total = points.size
                    val daysAgo = total - 1 - activeIdx
                    val date = java.time.LocalDate.now().minusDays(daysAgo.toLong())
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MM월 dd일")
                    date.format(formatter)
                }

                val formattedPriceTapped = if (isKoreanStock) {
                    df.format(priceTapped.toInt()) + "원"
                } else {
                    "$" + String.format("%.2f", priceTapped)
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(
                            x = if (activeIdx > points.size / 2) (-60).dp else 60.dp,
                            y = 12.dp
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = dateLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedPriceTapped,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticBox(
    title: String,
    content: String,
    iconSymbol: String,
    themeColor: Color
) {
    AccordionCard(
        title = title,
        iconSymbol = iconSymbol,
        themeColor = themeColor,
        initiallyExpanded = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = content,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp
        )
    }
}
