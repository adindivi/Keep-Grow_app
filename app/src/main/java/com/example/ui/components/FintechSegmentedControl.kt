package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.FintechBadgeBackground
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray500
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
 * 클린 화이트 미니멀 캡슐 세그먼트 컨트롤
 *
 * - 아이콘 및 과도한 슬라이딩 애니메이션 배제
 * - 배경을 아예 투명으로 빼고, 얇은 테두리만 두르는 방식
 * - 활성 탭: 화이트 배경 + 미세 1px 라이트 그레이 테두리(TossGray200)의 라운드 캡슐
 * - 비활성 탭: 투명 배경 + 미세 테두리 + 차분한 텍스트
 * - 텍스트 찌그러짐 방지 규격 (weight 1f, maxLines 1, softWrap false)
 */
@Composable
fun FintechSegmentedControl(
    items: List<SegmentTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerHeight: Dp = 40.dp,
    capsuleCornerRadius: Dp = 100.dp,
    activeBackgroundColor: Color = FintechBadgeBackground,
    activeBorderColor: Color = TossGray200,
    activeTextColor: Color = TossGray900,
    inactiveBorderColor: Color = TossGray200.copy(alpha = 0.6f),
    inactiveTextColor: Color = TossGray500
) {
    if (items.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }
            val shape = RoundedCornerShape(capsuleCornerRadius)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (isSelected) {
                            Modifier.shadow(elevation = 1.dp, shape = shape, clip = false)
                        } else {
                            Modifier
                        }
                    )
                    .clip(shape)
                    .background(if (isSelected) activeBackgroundColor else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) activeBorderColor else inactiveBorderColor,
                        shape = shape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onTabSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
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
                                color = if (isSelected) Color.White else TossGray500,
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
