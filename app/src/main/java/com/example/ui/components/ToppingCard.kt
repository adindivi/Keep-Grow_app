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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ToppingType(
    val title: String,
    val iconEmoji: String,
    val bgColor: Color,
    val accentColor: Color,
    val textColor: Color
) {
    SUCCESS(
        title = "성과 달성",
        iconEmoji = "🏆",
        bgColor = Color(0xFFF1F8E9),
        accentColor = Color(0xFF4CAF50),
        textColor = Color(0xFF2E7D32)
    ),
    WARNING(
        title = "변동성 주의",
        iconEmoji = "⚠️",
        bgColor = Color(0xFFFFF8E1),
        accentColor = Color(0xFFFF9800),
        textColor = Color(0xFFE65100)
    ),
    INFO(
        title = "스마트 가이드",
        iconEmoji = "ℹ️",
        bgColor = Color(0xFFE3F2FD),
        accentColor = Color(0xFF2196F3),
        textColor = Color(0xFF1565C0)
    ),
    TIP(
        title = "투자 꿀팁",
        iconEmoji = "💡",
        bgColor = Color(0xFFF3E5F5),
        accentColor = Color(0xFF9C27B0),
        textColor = Color(0xFF6A1B9A)
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
                .border(1.dp, type.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            color = type.bgColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(type.accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = type.iconEmoji, fontSize = 17.sp)
                }

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
                        color = Color(0xFF263238),
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
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = type.textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
