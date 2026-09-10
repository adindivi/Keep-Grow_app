package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ToppingCard
import com.example.ui.components.ToppingType
import com.example.ui.util.debouncedClickable
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ProfileType
import com.example.ui.viewmodel.ScreenTab

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProfile by viewModel.selectedProfile.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 1. Welcome Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Text(
                    text = "당신의 투자 성향을 선택하세요",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "반가워요, ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "자산가님!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 1.5 Contextual Profile Topping Card
        item {
            when (selectedProfile) {
                ProfileType.MARATHON -> {
                    ToppingCard(
                        type = ToppingType.INFO,
                        title = "마라톤 성향 선택됨 🏃",
                        message = "12개월 기준 지속 상승 확률 60% 이상 & 급등락을 최소화한 장기 우상향 종목으로 스크리너가 자동 세팅됩니다.",
                        actionText = "추천 종목 보러가기 👉",
                        onActionClick = { viewModel.selectTab(ScreenTab.STOCKS) }
                    )
                }
                ProfileType.ROCKET -> {
                    ToppingCard(
                        type = ToppingType.WARNING,
                        title = "로켓 성향 선택됨 🚀",
                        message = "3개월 기준 높은 상승 모멘텀과 고수익을 겨냥합니다. 큰 변동성이 수반되므로 분할 매수 원칙을 꼭 지켜주세요!",
                        actionText = "고성장 스크리너 열기 👉",
                        onActionClick = { viewModel.selectTab(ScreenTab.STOCKS) }
                    )
                }
                ProfileType.SLEEP -> {
                    ToppingCard(
                        type = ToppingType.TIP,
                        title = "꿀잠 성향 선택됨 🛌",
                        message = "일일 변동폭이 1.5% 이내로 안정적이며 꾸준히 배당과 가치가 성장하는 우량 자산 위주로 편안하게 투자합니다.",
                        actionText = "저변동 종목 보기 👉",
                        onActionClick = { viewModel.selectTab(ScreenTab.STOCKS) }
                    )
                }
            }
        }

        // 2. Bento Grid: Profile Selectors
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileBentoCard(
                    profile = ProfileType.MARATHON,
                    selected = selectedProfile == ProfileType.MARATHON,
                    title = "마라톤",
                    subtitle = "안전하게, 꾸준히",
                    tags = listOf("#안정지향", "#장기투자", "#자산배분"),
                    backgroundColor = Color(0xFF0058bc).copy(alpha = 0.08f),
                    accentColor = Color(0xFF0058bc),
                    iconSymbol = "🏃",
                    onSelect = { viewModel.selectProfileAndSave(ProfileType.MARATHON) }
                )

                ProfileBentoCard(
                    profile = ProfileType.ROCKET,
                    selected = selectedProfile == ProfileType.ROCKET,
                    title = "로켓",
                    subtitle = "높은 수익, 높은 위험",
                    tags = listOf("#고수익", "#성장주", "#하이리스크"),
                    backgroundColor = Color(0xFF8a2bb9).copy(alpha = 0.08f),
                    accentColor = Color(0xFF8a2bb9),
                    iconSymbol = "🚀",
                    onSelect = { viewModel.selectProfileAndSave(ProfileType.ROCKET) }
                )

                ProfileBentoCard(
                    profile = ProfileType.SLEEP,
                    selected = selectedProfile == ProfileType.SLEEP,
                    title = "꿀잠",
                    subtitle = "편안하게, 흔들림 없이",
                    tags = listOf("#배당성장", "#저변동", "#심리안정"),
                    backgroundColor = Color(0xFF4c4aca).copy(alpha = 0.08f),
                    accentColor = Color(0xFF4c4aca),
                    iconSymbol = "🛌",
                    onSelect = { viewModel.selectProfileAndSave(ProfileType.SLEEP) }
                )
            }
        }

        // 3. Market Indicators
        item {
            Column {
                Text(
                    text = "오늘의 시장 지표",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MarketIndicatorCard(
                        title = "S&P 500",
                        value = "5,137.08",
                        change = "+0.80%",
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                    MarketIndicatorCard(
                        title = "NASDAQ",
                        value = "16,274.94",
                        change = "+1.14%",
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Call-to-action Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF3A3D40), Color(0xFF181B1E))
                        )
                    )
                    .debouncedClickable { viewModel.selectTab(ScreenTab.STOCKS) }
                    .padding(20.dp)
            ) {
                // Background artistic sphere simulation using subtle circular overlays
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = 180.dp, y = 80.dp)
                        .background(Color(0xFF4c4aca).copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 240.dp, y = (-20).dp)
                        .background(Color(0xFF0070eb).copy(alpha = 0.25f), CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "나만의 맞춤형 포트폴리오를 구성해보세요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "선택하신 성향(${selectedProfile.displayName})을 바탕으로 AI가 최적의 종목을 추천합니다.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileBentoCard(
    profile: ProfileType,
    selected: Boolean,
    title: String,
    subtitle: String,
    tags: List<String>,
    backgroundColor: Color,
    accentColor: Color,
    iconSymbol: String,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .debouncedClickable { onSelect() }
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconSymbol, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "선택됨",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketIndicatorCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "trend",
                        tint = if (isPositive) Color(0xFFBA1A1A) else Color(0xFF0058bc),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = change,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFFBA1A1A) else Color(0xFF0058bc)
                    )
                }
            }
        }
    }
}
