package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TossGray100
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
import com.example.ui.theme.TossGray900

/**
 * 세그먼트 탭 항목 모델
 */
data class SegmentTabItem(
    val title: String,
    val icon: ImageVector? = null,
    val badgeText: String? = null
)

/**
 * 삼성 One UI 및 토스 공식 앱 스타일의 물리 스프링 슬라이딩 인디케이터 세그먼트 컨트롤.
 *
 * - 스프링 물리 기반 슬라이딩 캡슐 인디케이터 (Sliding Pill Spring Indicator)
 * - 텍스트 찌그러짐 방지 규격 (weight 1f, maxLines 1, softWrap false)
 * - 화이트 캡슐 + 1px TossGray200 미세 테두리 + 은은한 그림자
 */
@Composable
fun FintechSegmentedControl(
    items: List<SegmentTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerHeight: Dp = 44.dp,
    containerColor: Color = TossGray100,
    indicatorColor: Color = Color.White,
    activeTextColor: Color = TossGray900,
    inactiveTextColor: Color = TossGray600,
    activeIconTint: Color = TossGray900,
    inactiveIconTint: Color = TossGray600
) {
    if (items.isEmpty()) return

    // 0f ~ (items.size - 1)f 사이를 부드럽게 미끄러지는 물리 스프링 애니메이션
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.coerceIn(0, items.size - 1).toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, // 은은한 탄성 바운스
            stiffness = Spring.StiffnessMediumLow       // 부드러운 가속과 감속
        ),
        label = "segment_indicator_slide"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(3.dp)
    ) {
        val tabWidth = maxWidth / items.size
        val indicatorOffset = tabWidth * animatedIndex

        // 1. 스프링 물리 슬라이딩 인디케이터 캡슐
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(9.dp), clip = false)
                .clip(RoundedCornerShape(9.dp))
                .background(indicatorColor)
                .border(width = 1.dp, color = TossGray200, shape = RoundedCornerShape(9.dp))
        )

        // 2. 탭 항목 레이어
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onTabSelected(index)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) activeIconTint else inactiveIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }

                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) activeTextColor else inactiveTextColor,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )

                    if (item.badgeText != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) TossGray900 else TossGray200)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TossGray600,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
