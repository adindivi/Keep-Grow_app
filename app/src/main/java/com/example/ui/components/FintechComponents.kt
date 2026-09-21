package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FintechBadgeBackground
import com.example.ui.theme.FintechBadgeBorder
import com.example.ui.theme.TossGray100
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
import com.example.ui.theme.TossGreen
import com.example.ui.theme.TossGreen50
import com.example.ui.theme.TossRed
import com.example.ui.theme.TossRed50

/**
 * 핀테크 표준 아이콘 배지 컴포넌트
 * 화이트 배경 + 1px TossGray200 테두리 + 12dp 라운드 캡슐
 * 토스/네이버/카카오페이 수준의 아이콘 컨테이너
 */
@Composable
fun FintechIconBadge(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 12.dp,
    tint: Color = TossGray600,
    backgroundColor: Color = FintechBadgeBackground,
    borderColor: Color = FintechBadgeBorder,
    elevation: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = elevation, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * 핀테크 텍스트 아바타 배지 (종목 티커 첫 글자 등)
 * 화이트 배경 + 1px TossGray200 테두리 + 라운드 캡슐
 */
@Composable
fun FintechTextBadge(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 12.dp,
    textColor: Color = TossGray600,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    backgroundColor: Color = FintechBadgeBackground,
    borderColor: Color = FintechBadgeBorder,
    elevation: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = elevation, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

/**
 * 핀테크 해시태그 필 태그 (캡슐형)
 * 화이트 배경 + 1px TossGray200 테두리
 */
@Composable
fun FintechPillTag(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = TossGray600,
    fontSize: TextUnit = 11.sp,
    backgroundColor: Color = FintechBadgeBackground,
    borderColor: Color = TossGray200
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 핀테크 주가 변동 캡슐 배지
 * 수익(초록) / 손실(빨간) / 중립(회색) 3종 자동 분기
 */
@Composable
fun FintechStockChangeBadge(
    changeText: String,
    isPositive: Boolean?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp
) {
    val (bgColor, textColor) = when (isPositive) {
        true  -> TossGreen50 to TossGreen
        false -> TossRed50   to TossRed
        null  -> TossGray100 to TossGray600
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = changeText,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
