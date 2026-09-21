package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FintechStockChangeBadge
import com.example.ui.components.FintechTextBadge
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
import com.example.ui.theme.TossGray900
import com.example.ui.viewmodel.HighGrowthStock
import com.example.ui.viewmodel.HighGrowthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighGrowthScreenerScreen(
    viewModel: HighGrowthViewModel = viewModel()
) {
    val stocks by viewModel.stocks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.errorMessage.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "확인",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "고성장 24 스크리너",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TossGray900
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TossGray900
                ),
                actions = {
                    // ── Fintech 새로고침 버튼 (Outlined 아이콘) ──
                    IconButton(
                        onClick = { viewModel.fetchQuotes() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "새로고침",
                                tint = TossGray600
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchQuotes() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(stocks) { stock ->
                    FintechStockItemCard(stock = stock, onRetry = { viewModel.fetchQuotes() })
                }
            }
        }
    }
}

/**
 * 핀테크 스타일 종목 카드
 * 화이트 배경 + 1px TossGray200 테두리 + FintechTextBadge + FintechStockChangeBadge
 */
@Composable
fun FintechStockItemCard(
    stock: HighGrowthStock,
    onRetry: () -> Unit = {}
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, TossGray200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 핀테크 텍스트 아바타 배지 (화이트 + 1px TossGray200) ──
            FintechTextBadge(
                text = stock.symbol.take(2),
                size = 46.dp,
                cornerRadius = 12.dp,
                textColor = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // ── 종목 정보 ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TossGray900,
                    maxLines = 1
                )
                Text(
                    text = stock.name,
                    fontSize = 12.sp,
                    color = TossGray600,
                    maxLines = 1
                )
            }

            // ── 가격 및 변동 캡슐 배지 ──
            when {
                stock.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                stock.error != null -> {
                    Column(horizontalAlignment = Alignment.End) {
                        FintechStockChangeBadge(
                            changeText = "일시 오류",
                            isPositive = null,
                            fontSize = 11.sp
                        )
                        TextButton(
                            onClick = onRetry,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("재시도", fontSize = 11.sp)
                        }
                    }
                }
                stock.quote != null -> {
                    val currentPrice = stock.quote.currentPrice ?: 0.0
                    val percentChange = stock.quote.percentChange ?: 0.0
                    val isPositive = percentChange >= 0
                    val sign = if (isPositive) "+" else ""

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format("%.2f", currentPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TossGray900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // ── 수익/손실 색상 캡슐 배지 ──
                        FintechStockChangeBadge(
                            changeText = "$sign${String.format("%.2f", percentChange)}%",
                            isPositive = isPositive
                        )
                    }
                }
            }
        }
    }
}
