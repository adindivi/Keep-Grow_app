package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FintechIconBadge
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
import com.example.ui.theme.TossGray900

enum class ToppingType(
    val title: String,
    val icon: ImageVector,
    val bgColor: Color,
    val accentColor: Color,
    val textColor: Color
) {
    SUCCESS(
        title = "성과 달성",
        icon = Icons.Outlined.CheckCircle,
        bgColor = Color(0xFFF0FDF4),
        accentColor = Color(0xFF16A34A),
        textColor = Color(0xFF15803D)
    ),
    WARNING(
        title = "변동성 주의",
        icon = Icons.Outlined.WarningAmber,
        bgColor = Color(0xFFFFFBEB),
        accentColor = Color(0xFFD97706),
        textColor = Color(0xFFB45309)
    ),
    INFO(
        title = "스마트 가이드",
        icon = Icons.Outlined.Info,
        bgColor = Color(0xFFEFF6FF),
        accentColor = Color(0xFF2563EB),
        textColor = Color(0xFF1D4ED8)
    ),
    TIP(
        title = "투자 인사이트",
        icon = Icons.Outlined.Lightbulb,
        bgColor = Color(0xFFFAF5FF),
        accentColor = Color(0xFF9333EA),
        textColor = Color(0xFF7E22CE)
    )
}

/**
 * Modern Contextual Topping Card for helpful tips, market alerts, and feedback.
 */
@Composable
fun ToppingCard(
    type: ToppingType,
    title: String = type.title,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, TossGray200, RoundedCornerShape(16.dp)),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ── 핀테크 표준 벡터 아이콘 배지 (화이트/틴트 배경 + 1px TossGray200 테두리) ──
                FintechIconBadge(
                    icon = type.icon,
                    contentDescription = title,
                    size = 36.dp,
                    iconSize = 18.dp,
                    cornerRadius = 10.dp,
                    tint = type.accentColor,
                    backgroundColor = type.bgColor,
                    borderColor = TossGray200,
                    elevation = 0.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = type.textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = TossGray900,
                        lineHeight = 17.sp
                    )

                    if (actionText != null && onActionClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = actionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = type.accentColor,
                            modifier = Modifier.clickable { onActionClick() }
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "닫기",
                            tint = TossGray600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
