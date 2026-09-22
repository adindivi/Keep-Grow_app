package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.RocketLaunch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FintechIconBadge
import com.example.ui.components.FintechPillTag
import com.example.ui.components.ToppingCard
import com.example.ui.components.ToppingType
import com.example.ui.theme.FintechBadgeBorder
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Greeting
        item {
            Column {
                Text(
                    text = "환영합니다!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "나에게 맞는 최적의 투자 성향을 선택하세요.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1.5 Contextual Profile Topping Card
        item {
            when (selectedProfile) {
                ProfileType.MARATHON -> {
                    ToppingCard(
                        type = ToppingType.INFO,
                        title = "마라톤 성향 선택됨",
                        message = "12개월 기준 지속 상승 확률 60% 이상 & 급등락을 최소화한 장기 우상향 종목으로 스크리너가 자동 세팅됩니다.",
                        actionText = "추천 종목 보러가기",
                        onActionClick = { viewModel.selectTab(ScreenTab.STOCKS) }
                    )
                }
                ProfileType.ROCKET -> {
                    ToppingCard(
                        type = ToppingType.WARNING,
                        title = "로켓 성향 선택됨",
                        message = "3개월 기준 높은 상승 모멘텀과 고수익을 겨냥합니다. 큰 변동성이 수반되므로 분할 매수 원칙을 꼭 지켜주세요!",
                        actionText = "고성장 스크리너 열기",
                        onActionClick = { viewModel.selectTab(ScreenTab.STOCKS) }
                    )
                }
                ProfileType.SLEEP -> {
                    ToppingCard(
                        type = ToppingType.TIP,
                        title = "꿀잠 성향 선택됨",
                        message = "일일 변동폭이 1.5% 이내로 안정적이며 꾸준히 배당과 가치가 성장하는 우량 자산 위주로 편안하게 투자합니다.",
                        actionText = "저변동 종목 보기",
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
                    accentColor = Color(0xFF0058bc),
                    icon = Icons.AutoMirrored.Outlined.DirectionsRun,
                    iconTint = Color(0xFF0058bc),
                    onSelect = { viewModel.selectProfileAndSave(ProfileType.MARATHON) }
                )

                ProfileBentoCard(
                    profile = ProfileType.ROCKET,
                    selected = selectedProfile == ProfileType.ROCKET,
                    title = "로켓",
                    subtitle = "높은 수익, 높은 위험",
                    tags = listOf("#고수익", "#성장주", "#하이리스크"),
                    accentColor = Color(0xFF8a2bb9),
                    icon = Icons.Outlined.RocketLaunch,
                    iconTint = Color(0xFF8a2bb9),
                    onSelect = { viewModel.selectProfileAndSave(ProfileType.ROCKET) }
                )

                ProfileBentoCard(
                    profile = ProfileType.SLEEP,
                    selected = selectedProfile == ProfileType.SLEEP,
                    title = "꿀잠",
                    subtitle = "편안하게, 흔들림 없이",
                    tags = listOf("#배당성장", "#저변동", "#심리안정"),
                    accentColor = Color(0xFF4c4aca),
                    icon = Icons.Outlined.Bedtime,
                    iconTint = Color(0xFF4c4aca),
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
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2C3036), Color(0xFF181B1E))
                        )
                    )
                    .debouncedClickable { viewModel.selectTab(ScreenTab.STOCKS) }
                    .padding(20.dp)
            ) {
                // 우측 상단 바로가기 핀테크 배지
                FintechIconBadge(
                    icon = Icons.Outlined.AutoGraph,
                    contentDescription = "포트폴리오 구성",
                    size = 40.dp,
                    iconSize = 20.dp,
                    cornerRadius = 12.dp,
                    tint = Color.White,
                    backgroundColor = Color.White.copy(alpha = 0.12f),
                    borderColor = Color.White.copy(alpha = 0.2f),
                    elevation = 0.dp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "나만의 맞춤형 포트폴리오를 구성해보세요",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "선택하신 성향(${selectedProfile.displayName})을 바탕으로 AI가 최적의 종목을 추천합니다.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
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
    accentColor: Color,
    icon: ImageVector,
    iconTint: Color,
    onSelect: () -> Unit
) {
    // 선택 시: accentColor 2dp 테두리, 비선택 시: TossGray200 1dp 테두리
    val borderStroke = if (selected)
        androidx.compose.foundation.BorderStroke(2.dp, accentColor)
    else
        androidx.compose.foundation.BorderStroke(1.dp, TossGray200)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .debouncedClickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 3.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 핀테크 벡터 아이콘 배지 (화이트 + 1px TossGray200 테두리) ──
            FintechIconBadge(
                icon = icon,
                contentDescription = title,
                size = 52.dp,
                iconSize = 26.dp,
                cornerRadius = 14.dp,
                tint = iconTint,
                backgroundColor = accentColor.copy(alpha = 0.06f),
                borderColor = accentColor.copy(alpha = 0.20f),
                elevation = if (selected) 3.dp else 1.dp
            )

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
                        // 선택됨 뱃지: accentColor 배경 + 흰 텍스트
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TossGray600
                )
                Spacer(modifier = Modifier.height(10.dp))
                // ── FintechPillTag: 화이트 배경 + 1px TossGray200 테두리 캡슐 ──
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        FintechPillTag(
                            text = tag,
                            textColor = TossGray600
                        )
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
    val changeBgColor = if (isPositive) Color(0xFFBA1A1A).copy(alpha = 0.08f) else Color(0xFF0058bc).copy(alpha = 0.08f)
    val changeTextColor = if (isPositive) Color(0xFFBA1A1A) else Color(0xFF0058bc)

    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TossGray200),
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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(changeBgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "trend",
                            tint = changeTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = change,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeTextColor,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
