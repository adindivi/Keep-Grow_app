package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import com.example.data.database.PortfolioStock
import com.example.data.database.WatchlistStock
import com.example.ui.viewmodel.StockData
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.components.*
import com.example.ui.theme.FintechBadgeBackground
import com.example.ui.theme.TossGray200
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab
import java.text.DecimalFormat

@Composable
fun PortfolioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val portfolioStocks by viewModel.portfolioStocks.collectAsState()
    val isOptimized by viewModel.isOptimized.collectAsState()
    val watchlistStocks by viewModel.watchlistStocks.collectAsState()
    val userCash by viewModel.userCash.collectAsState()

    val exchangeRate by viewModel.exchangeRate.collectAsState()
    val exchangeRateTime by viewModel.exchangeRateTime.collectAsState()
    val isExchangeRateLoading by viewModel.isExchangeRateLoading.collectAsState()
    val isPortfolioUpdating by viewModel.isPortfolioUpdating.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditCashDialog by remember { mutableStateOf(false) }
    var showRebalanceDialog by remember { mutableStateOf(false) }
    var showAddWatchlistDialog by remember { mutableStateOf(false) }

    // Watchlist average daily gain calculation
    val watchlistDailyGain = remember(watchlistStocks) {
        if (watchlistStocks.isEmpty()) 0.0 else watchlistStocks.map { it.changePct }.average()
    }

    // Dynamic Total Assets Calculation based on database values + userCash!
    val totalAssetsValue = remember(portfolioStocks, userCash) {
        portfolioStocks.sumOf { it.price * it.quantity } + userCash
    }

    val prevDayValue = remember(portfolioStocks, userCash) {
        portfolioStocks.sumOf {
            val prevPrice = it.price / (1.0 + (it.changePct / 100.0))
            prevPrice * it.quantity
        } + userCash
    }

    val dailyChangePercentage = remember(totalAssetsValue, prevDayValue) {
        if (prevDayValue == 0.0) 0.0 else ((totalAssetsValue - prevDayValue) / prevDayValue) * 100.0
    }

    val totalReturnPercentage = remember(portfolioStocks) {
        // Average cumulative return simulation
        val weightedSum = portfolioStocks.sumOf { it.changePct * (it.price * it.quantity) }
        val grandTotal = portfolioStocks.sumOf { it.price * it.quantity }
        if (grandTotal == 0.0) 12.4 else weightedSum / grandTotal
    }

    val df = remember { DecimalFormat("#,###") }
    val pf = remember { DecimalFormat("+0.0%;-0.0%") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 0. Contextual Topping Card
        item {
            when {
                portfolioStocks.isEmpty() -> {
                    ToppingCard(
                        type = ToppingType.INFO,
                        title = "첫 포트폴리오 만들기",
                        message = "보유하신 주식을 등록하면 AI가 변동성을 진단하고 최적의 투자 비중을 계산해드려요.",
                        actionText = "+ 종목 직접 추가하기",
                        onActionClick = { showAddDialog = true }
                    )
                }
                userCash <= 0.0 -> {
                    ToppingCard(
                        type = ToppingType.TIP,
                        title = "현금 자산 설정 팁",
                        message = "보유 현금을 입력하면 하락장 방어와 리밸런싱을 위한 주식-현금 황금 비율을 제안합니다.",
                        actionText = "💵 보유 현금 설정하기",
                        onActionClick = { showEditCashDialog = true }
                    )
                }
                isOptimized -> {
                    ToppingCard(
                        type = ToppingType.SUCCESS,
                        title = "포트폴리오 최적화 완료",
                        message = "선택하신 투자 성향에 맞추어 주식 비중이 안정적으로 분산 설계되었습니다.",
                        actionText = "리밸런싱 제안 다시 보기",
                        onActionClick = { showRebalanceDialog = true }
                    )
                }
                else -> {
                    ToppingCard(
                        type = ToppingType.TIP,
                        title = "AI 포트폴리오 진단",
                        message = "하단의 'AI 진단 및 추천 비중 적용'을 누르면 성향에 맞춘 최적 비중을 원클릭으로 계산합니다.",
                        actionText = "최적화 확인하기",
                        onActionClick = { showRebalanceDialog = true }
                    )
                }
            }
        }

        // 1. Dashboard Block (Balance Tracker)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "총 자산 가치",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isPortfolioUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                IconButton(
                                    onClick = { viewModel.updatePortfolioAndWatchlistPrices(forceRefresh = true) },
                                    enabled = !isPortfolioUpdating,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "시세 갱신",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showEditCashDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "보유 현금 설정",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "현금: ₩${df.format(userCash)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₩${df.format(totalAssetsValue)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val positiveChange = dailyChangePercentage >= 0
                    val trendColor = if (positiveChange) Color(0xFFBA1A1A) else Color(0xFF0058bc)
                    val trendIcon = if (positiveChange) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(trendColor.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = trendIcon,
                                    contentDescription = "trend",
                                    tint = trendColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = pf.format(dailyChangePercentage / 100.0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = trendColor
                                )
                            }
                        }

                        Text(
                            text = "누적 수익률 ${pf.format(totalReturnPercentage / 100.0)}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = "환율",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "실시간 원/달러 환율 (USD/KRW)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = exchangeRateTime?.let { "야후 기준: $it" } ?: "야후 파이낸스 실시간 정보",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = exchangeRate?.let { "₩${DecimalFormat("#,##0.00").format(it)}" } ?: "₩1,385.00",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isExchangeRateLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                IconButton(
                                    onClick = { viewModel.fetchExchangeRate() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "환율 갱신",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Asset Allocation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "자산 구성",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Simulated Allocation Bars (Highly dynamic: shifts if optimized!)
                    val localStockRatio = if (isOptimized) 0.38f else 0.45f
                    val globalStockRatio = if (isOptimized) 0.22f else 0.25f
                    val bondRatio = if (isOptimized) 0.25f else 0.20f
                    val otherRatio = if (isOptimized) 0.15f else 0.10f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AnimatedBar(ratio = localStockRatio, color = MaterialTheme.colorScheme.primary, label = "국내주식", modifier = Modifier.weight(1f))
                        AnimatedBar(ratio = globalStockRatio, color = MaterialTheme.colorScheme.secondary, label = "해외주식", modifier = Modifier.weight(1f))
                        AnimatedBar(ratio = bondRatio, color = MaterialTheme.colorScheme.tertiary, label = "채권/현금", modifier = Modifier.weight(1f))
                        AnimatedBar(ratio = otherRatio, color = Color(0xFFC1C6D7), label = "기타", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Text legends
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            LegendRow(color = MaterialTheme.colorScheme.primary, text = "국내주식 ${if (isOptimized) "38%" else "45%"}", modifier = Modifier.weight(1f))
                            LegendRow(color = MaterialTheme.colorScheme.secondary, text = "해외주식 ${if (isOptimized) "22%" else "25%"}", modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            LegendRow(color = MaterialTheme.colorScheme.tertiary, text = "채권/현금 ${if (isOptimized) "25%" else "20%"}", modifier = Modifier.weight(1f))
                            LegendRow(color = Color(0xFFC1C6D7), text = "기타 ${if (isOptimized) "15%" else "10%"}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 3. AI Advisory Glass Container
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI advice",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "AI 포트폴리오 최적화 제안",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isOptimized) {
                            "제안된 전략적 포트폴리오 리배런싱 비율이 안전하게 분산 배치되었습니다. 기술주 비중이 조율되고 헬스케어 및 가치 방어주 비중이 8% 상향 확보되었습니다."
                        } else {
                            "현재 포트폴리오가 기술주에 다소 치우쳐 있습니다. 안정성을 위해 금융 및 헬스케어 비중을 8% 확대하는 것을 추천합니다. 리밸런싱을 통해 위험 대비 수익률을 1.2%p 개선할 수 있습니다."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.applyOptimization() },
                        enabled = !isOptimized,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isOptimized) "최적화 완료됨" else "제안 적용하기",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- 4. Watchlist (관심 종목) Section ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "관심 종목 (Watchlist)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show Watchlist Average Gain
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (watchlistDailyGain >= 0) Color(0xFFBA1A1A).copy(alpha = 0.1f)
                                    else Color(0xFF0058bc).copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "평균: ${if (watchlistDailyGain >= 0) "+" else ""}${String.format("%.2f", watchlistDailyGain)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (watchlistDailyGain >= 0) Color(0xFFBA1A1A) else Color(0xFF0058bc)
                            )
                        }

                        // Add Button for Watchlist
                        IconButton(
                            onClick = { showAddWatchlistDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "관심 종목 추가",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (watchlistStocks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .clickable { viewModel.selectTab(ScreenTab.STOCKS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Empty Watchlist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "관심 종목이 없습니다. '종목 스크리닝'에서 별표를 눌러 추가해보세요.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        watchlistStocks.forEach { stock ->
                            WatchlistStockRow(
                                stock = stock,
                                onRemove = { viewModel.removeFromWatchlist(stock.ticker) },
                                onClick = {
                                    viewModel.viewStockDetails(
                                        StockData(
                                            name = stock.name,
                                            ticker = stock.ticker,
                                            price = DecimalFormat("#,###").format(stock.price),
                                            change = "${if (stock.changePct >= 0) "+" else ""}${stock.changePct}%",
                                            isPositive = stock.changePct >= 0,
                                            trend = if (stock.changePct > 0) "up" else if (stock.changePct < 0) "down" else "flat",
                                            prices = listOf(15f, 15f, 15f, 15f, 15f, 15f)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Owned Stocks title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "소유 종목",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showRebalanceDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "리밸런싱",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("자산 리밸런싱", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Grid list of Owned Stocks
        // Using manual chunking or Column layout since Nested Grids inside LazyColumn are forbidden/hazardous
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Chunk portfolio stocks for 2-column display
                portfolioStocks.chunked(2).forEach { rowStocks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowStocks.forEach { stock ->
                            OwnedStockCard(
                                stock = stock,
                                onRemove = { viewModel.removeStock(stock.id) },
                                onClick = {
                                    viewModel.viewStockDetails(
                                        StockData(
                                            name = stock.name,
                                            ticker = stock.ticker,
                                            price = DecimalFormat("#,###").format(stock.price),
                                            change = "${if (stock.changePct >= 0) "+" else ""}${String.format("%.2f", stock.changePct)}%",
                                            isPositive = stock.changePct >= 0,
                                            trend = if (stock.changePct > 0) "up" else if (stock.changePct < 0) "down" else "flat",
                                            prices = listOf(15f, 15f, 15f, 15f, 15f, 15f)
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty slot with Add Stock button if row is incomplete
                        if (rowStocks.size < 2) {
                            AddStockPlaceholderCard(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                // If perfectly chunked, show add button at the bottom as its own block
                if (portfolioStocks.size % 2 == 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AddStockPlaceholderCard(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(0.5f)
                        )
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }

        // 6. Market News & Reports
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "시장 주요 뉴스",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "01" to "연준, 금리 동결 시사... 기술주 반등세 지속 전망",
                        "02" to "반도체 업황 회복 본격화, 삼성전자 실적 기대치 상회",
                        "03" to "환율 하락세 안정... 해외 주식 투자자 환리스크 주의"
                    ).forEach { (idx, title) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = idx,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "포트폴리오 리포트",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimal mock 3D Report box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Report",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "PDF Analytics",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "리포트 읽어보기 →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }

    // Modal Add Dialog
    if (showAddDialog) {
        AddStockDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, ticker, price, change, qty ->
                viewModel.addManualStock(name, ticker, price, change, qty)
                showAddDialog = false
            }
        )
    }

    if (showEditCashDialog) {
        EditCashDialog(
            initialCash = userCash,
            onDismiss = { showEditCashDialog = false },
            onConfirm = { cash ->
                viewModel.saveUserCash(cash)
                showEditCashDialog = false
            }
        )
    }

    if (showAddWatchlistDialog) {
        AddWatchlistDialog(
            onDismiss = { showAddWatchlistDialog = false },
            onConfirm = { name, ticker, price, change, isPositive ->
                viewModel.addToWatchlist(name, ticker, price, change, isPositive)
                showAddWatchlistDialog = false
            }
        )
    }
}

@Composable
fun AnimatedBar(
    ratio: Float,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedHeight by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 800)
    )

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(animatedHeight)
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LegendRow(
    color: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OwnedStockCard(
    stock: PortfolioStock,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val df = remember { DecimalFormat("#,###") }
    val pf = remember { DecimalFormat("+0.0%;-0.0%") }

    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Delete badge if custom Stock added by user
            if (stock.isCustom) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = stock.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = TextStyle(lineBreak = LineBreak.Paragraph)
                        )
                        Text(
                            text = stock.ticker,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val changeColor = if (stock.changePct >= 0) Color(0xFFBA1A1A) else Color(0xFF0058bc)
                    Text(
                        text = pf.format(stock.changePct / 100.0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor,
                        modifier = Modifier.padding(end = if (stock.isCustom) 16.dp else 0.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "평가금액", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₩${df.format(stock.price * stock.quantity)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "수량", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${stock.quantity} 주", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddStockPlaceholderCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = Color(0xFFC1C6D7).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "종목 추가하기",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AddStockDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var changeStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && ticker.isNotBlank() && (qtyStr.toIntOrNull() ?: 0) > 0

    ModernModalDialog(
        onDismissRequest = onDismiss,
        title = "소유 종목 추가",
        subtitle = "포트폴리오에 직접 매수한 주식을 등록합니다.",
        icon = Icons.Outlined.AddCard,
        confirmText = "등록하기",
        isConfirmEnabled = isValid,
        onConfirm = {
            val price = priceStr.toDoubleOrNull() ?: 0.0
            val change = changeStr.toDoubleOrNull() ?: 0.0
            val qty = qtyStr.toIntOrNull() ?: 0
            if (isValid) {
                onConfirm(name.trim(), ticker.trim().uppercase(), price, change, qty)
            }
        },
        dismissText = "취소",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeepGrowTextField(
                value = name,
                onValueChange = { name = it },
                label = "종목명",
                placeholder = "예: 애플, 삼성전자"
            )
            KeepGrowTextField(
                value = ticker,
                onValueChange = { ticker = it },
                label = "티커/종목코드",
                placeholder = "예: AAPL, 005930"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeepGrowTextField(
                    value = priceStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) priceStr = it },
                    label = "매수가/현재가",
                    placeholder = "244500",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                KeepGrowTextField(
                    value = changeStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' || ch == '-' }) changeStr = it },
                    label = "수익률 (%)",
                    placeholder = "12.4",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            KeepGrowTextField(
                value = qtyStr,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) qtyStr = it },
                label = "보유 수량 (주)",
                placeholder = "예: 50",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun WatchlistStockRow(
    stock: WatchlistStock,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val df = remember { DecimalFormat("#,###") }
    val changeColor = if (stock.changePct >= 0) Color(0xFFBA1A1A) else Color(0xFF0058bc)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val stockInitials = if (stock.ticker.all { it.isDigit() }) {
                    stock.name.take(2)
                } else {
                    stock.ticker.take(2)
                }
                FintechTextBadge(
                    text = stockInitials,
                    size = 36.dp,
                    cornerRadius = 8.dp,
                    fontSize = 12.sp,
                    textColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = FintechBadgeBackground,
                    borderColor = TossGray200
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stock.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        style = TextStyle(lineBreak = LineBreak.Paragraph)
                    )
                    Text(
                        text = stock.ticker,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₩${df.format(stock.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (stock.changePct >= 0) "+" else ""}${String.format("%.2f", stock.changePct)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "관심종목 삭제",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCashDialog(
    initialCash: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var cashStr by remember { mutableStateOf(if (initialCash > 0) initialCash.toLong().toString() else "") }
    val formatter = remember { DecimalFormat("#,###") }
    val parsedCash = cashStr.toDoubleOrNull() ?: 0.0

    ModernModalDialog(
        onDismissRequest = onDismiss,
        title = "보유 현금 설정",
        subtitle = "투자 포트폴리오에 반영할 현금 자산(원화)을 입력해주세요.",
        iconEmoji = "💵",
        confirmText = "설정 완료",
        onConfirm = {
            onConfirm(parsedCash)
        },
        dismissText = "취소",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KeepGrowTextField(
                value = cashStr,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) cashStr = it },
                label = "현금 자산 (₩)",
                placeholder = "예: 5000000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (cashStr.isNotBlank() && parsedCash > 0) {
                Text(
                    text = "입력 금액: ${formatter.format(parsedCash.toLong())}원",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AddWatchlistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var changeStr by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && ticker.isNotBlank()

    ModernModalDialog(
        onDismissRequest = onDismiss,
        title = "관심 종목 추가",
        subtitle = "관심 있게 지켜볼 주식을 등록하고 시세를 모니터링하세요.",
        iconEmoji = "⭐",
        confirmText = "등록하기",
        isConfirmEnabled = isValid,
        onConfirm = {
            val price = priceStr.toDoubleOrNull() ?: 0.0
            val change = changeStr.toDoubleOrNull() ?: 0.0
            if (isValid) {
                onConfirm(name.trim(), ticker.trim().uppercase(), price, change, change >= 0)
            }
        },
        dismissText = "취소",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeepGrowTextField(
                value = name,
                onValueChange = { name = it },
                label = "종목명",
                placeholder = "예: 삼성전자, NAVER"
            )
            KeepGrowTextField(
                value = ticker,
                onValueChange = { ticker = it },
                label = "티커/종목코드",
                placeholder = "예: 005930, 035420"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeepGrowTextField(
                    value = priceStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) priceStr = it },
                    label = "현재가",
                    placeholder = "73500",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                KeepGrowTextField(
                    value = changeStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' || ch == '-' }) changeStr = it },
                    label = "변동률 (%)",
                    placeholder = "1.5",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
