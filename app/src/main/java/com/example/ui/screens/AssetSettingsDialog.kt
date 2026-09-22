package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.FintechSegmentedControl
import com.example.ui.components.FintechTextBadge
import com.example.ui.components.SegmentTabItem
import com.example.ui.theme.FintechBadgeBackground
import com.example.ui.theme.TossGray200
import com.example.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userCash by viewModel.userCash.collectAsState()
    val portfolioStocks by viewModel.portfolioStocks.collectAsState()

    var cashInput by remember { mutableStateOf(userCash.toLong().toString()) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Track modifications to stock values locally
    val modifiedPrices = remember { mutableStateMapOf<Int, String>() }
    val modifiedQuantities = remember { mutableStateMapOf<Int, String>() }

    val formatter = remember { DecimalFormat("#,###") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .imePadding()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "쉬운 자산 및 투자금 설정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 3-Tab Segmented Control (삼성 One UI / 토스 스타일)
                var selectedTab by remember { mutableIntStateOf(0) }
                val tabItems = remember(portfolioStocks.size) {
                    listOf(
                        SegmentTabItem(
                            title = "현금 자산",
                            icon = Icons.Outlined.AccountBalanceWallet
                        ),
                        SegmentTabItem(
                            title = "보유 주식",
                            icon = Icons.AutoMirrored.Outlined.ShowChart,
                            badgeText = if (portfolioStocks.isNotEmpty()) "${portfolioStocks.size}" else null
                        ),
                        SegmentTabItem(
                            title = "투자 팁",
                            icon = Icons.Outlined.Lightbulb
                        )
                    )
                }

                FintechSegmentedControl(
                    items = tabItems,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content with Fluid Spring Horizontal Slide & Crossfade Transition
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { width -> (width * 0.35f).toInt() } + fadeIn(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) { width -> (-width * 0.35f).toInt() } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { width -> (-width * 0.35f).toInt() } + fadeIn(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) { width -> (width * 0.35f).toInt() } + fadeOut()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "asset_settings_tab_content"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> {
                            // ── TAB 1: 현금 자산 ────────────────────────────────────
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Middle Schooler Friendly Guidance Alert
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "지갑 속 현금을 입력해 두면, AI 비서가 최적의 투자 비중과 리밸런싱을 똑똑하게 계산해 줘요!",
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                // Cash Input Section
                                Column {
                                    Text(
                                        text = "내 지갑 속 현금 (원화)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = cashInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() }) {
                                                cashInput = input
                                            }
                                        },
                                        placeholder = { Text("가진 현금을 원화 단위로 써주세요", color = Color.Gray) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color.Black,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black,
                                            focusedContainerColor = Color(0xFFF8F9FA),
                                            unfocusedContainerColor = Color(0xFFF8F9FA),
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                    
                                    if (cashInput.isNotEmpty()) {
                                        val parsed = cashInput.toLongOrNull() ?: 0L
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "한눈에 보기: ${formatter.format(parsed)}원",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }

                                // Quick Cash Buttons
                                Column {
                                    Text(
                                        text = "빠른 금액 더하기",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(100_000L to "+10만", 500_000L to "+50만", 1_000_000L to "+100만", 5_000_000L to "+500만").forEach { (amt, label) ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                border = BorderStroke(1.dp, TossGray200),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val current = cashInput.toLongOrNull() ?: 0L
                                                        cashInput = (current + amt).toString()
                                                    }
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // ── TAB 2: 보유 주식 목록 ────────────────────────────────
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "보유 주식 (${portfolioStocks.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "단가 및 수량 인라인 수정",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (portfolioStocks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(1.dp, TossGray200, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "등록된 주식이 없어요!",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "주식 상세 화면에서 관심등록 후 여기에 등록해 보세요.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                } else {
                                    portfolioStocks.forEach { stock ->
                                        // Read current or modified values
                                        val currentPrice = modifiedPrices[stock.id] ?: stock.price.toInt().toString()
                                        val currentQty = modifiedQuantities[stock.id] ?: stock.quantity.toString()

                                        // Middle school friendly name mapping & Subtitle
                                        val tagline = when (stock.ticker.uppercase()) {
                                            "005930" -> "삼성전자 - 갤럭시 스마트폰과 가전 대표 브랜드"
                                            "000660" -> "SK하이닉스 - 고성능 AI 메모리 반도체 대표 제품"
                                            "NVDA" -> "엔비디아 - 인공지능용 최고 스펙 그래픽카드(GPU)"
                                            "AAPL" -> "애플 - 전세계가 사랑하는 아이폰과 맥북"
                                            "TSLA" -> "테슬라 - 미래 혁신을 그리는 대표 전기자동차"
                                            else -> "내가 아주 눈여겨보고 있는 특별한 기업"
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, TossGray200)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp)
                                            ) {
                                                // Stock Header Row
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    val stockInitials = if (stock.ticker.all { it.isDigit() }) {
                                                        stock.name.take(2)
                                                    } else {
                                                        stock.ticker.take(2)
                                                    }
                                                    FintechTextBadge(
                                                        text = stockInitials,
                                                        size = 36.dp,
                                                        cornerRadius = 10.dp,
                                                        fontSize = 12.sp,
                                                        textColor = MaterialTheme.colorScheme.primary,
                                                        backgroundColor = FintechBadgeBackground,
                                                        borderColor = TossGray200
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = stock.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = tagline,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeStock(stock.id)
                                                            Toast.makeText(context, "${stock.name} 삭제 완료", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Inputs Row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // Average Price TextField
                                                    OutlinedTextField(
                                                        value = currentPrice,
                                                        onValueChange = { input ->
                                                            if (input.all { it.isDigit() }) {
                                                                modifiedPrices[stock.id] = input
                                                            }
                                                        },
                                                        label = { Text("평단가 (원/달러)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1.1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        singleLine = true,
                                                        textStyle = androidx.compose.ui.text.TextStyle(
                                                            color = Color.Black,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.Black,
                                                            unfocusedTextColor = Color.Black,
                                                            cursorColor = Color.Black,
                                                            focusedContainerColor = Color(0xFFF8F9FA),
                                                            unfocusedContainerColor = Color(0xFFF8F9FA),
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                        )
                                                    )

                                                    // Quantity TextField
                                                    OutlinedTextField(
                                                        value = currentQty,
                                                        onValueChange = { input ->
                                                            if (input.all { it.isDigit() }) {
                                                                modifiedQuantities[stock.id] = input
                                                            }
                                                        },
                                                        label = { Text("수량 (개)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(0.9f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        singleLine = true,
                                                        textStyle = androidx.compose.ui.text.TextStyle(
                                                            color = Color.Black,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.Black,
                                                            unfocusedTextColor = Color.Black,
                                                            cursorColor = Color.Black,
                                                            focusedContainerColor = Color(0xFFF8F9FA),
                                                            unfocusedContainerColor = Color(0xFFF8F9FA),
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // ── TAB 3: 투자 팁 ───────────────────────────────────────
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "똑똑한 자산 관리 꿀팁",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                listOf(
                                    "💵 현금 비중 원칙" to "투자금의 20~30%는 항상 현금으로 보유하면 하락장에서도 저점 매수 기회를 잡을 수 있어요.",
                                    "📊 분할 매수 원칙" to "한 번에 전액을 매수하기보다 2~3회에 나누어 매수하면 평단가를 안정적으로 관리할 수 있습니다.",
                                    "🔄 주기적 리밸런싱" to "특정 주식이 너무 많이 올랐거나 비중이 과도해졌다면 목표 비중에 맞춰 일부 차익을 실현해 보세요.",
                                    "💾 실시간 저장" to "수정하신 단가나 수량, 현금은 하단의 '저장할래!' 버튼을 누르시면 안전하게 저장됩니다."
                                ).forEach { (tipTitle, tipDesc) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, TossGray200)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = tipTitle,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tipDesc,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("취소할래", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true

                            // 1. Save user cash
                            val cashParsed = cashInput.toDoubleOrNull() ?: 0.0
                            viewModel.saveUserCash(cashParsed)

                            // 2. Save modified stock prices and quantities
                            portfolioStocks.forEach { stock ->
                                val priceVal = modifiedPrices[stock.id]?.toDoubleOrNull() ?: stock.price
                                val qtyVal = modifiedQuantities[stock.id]?.toIntOrNull() ?: stock.quantity
                                viewModel.updateManualStock(stock.id, priceVal, qtyVal)
                            }

                            Toast.makeText(context, "자산이 안전하게 저장되었어요! 💾", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("저장할래!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
