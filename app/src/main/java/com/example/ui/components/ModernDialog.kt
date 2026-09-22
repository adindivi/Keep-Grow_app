package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.FintechIconBadge
import com.example.ui.theme.TossGray200

/**
 * Modern popup modal dialog with rounded corners, icon badge header,
 * clean typography, and customizable action buttons.
 */
@Composable
fun ModernModalDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconEmoji: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
    dismissText: String? = "취소",
    onDismiss: (() -> Unit)? = onDismissRequest,
    isConfirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 핀테크 표준 벡터 아이콘 배지 (화이트 배경 + 1px TossGray200 테두리) ──
                val displayIcon = icon ?: if (iconEmoji != null) Icons.Outlined.NotificationsActive else null
                if (displayIcon != null) {
                    FintechIconBadge(
                        icon = displayIcon,
                        contentDescription = title,
                        size = 56.dp,
                        iconSize = 28.dp,
                        cornerRadius = 16.dp,
                        tint = iconTint,
                        backgroundColor = Color.White,
                        borderColor = TossGray200,
                        elevation = 2.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Title & Subtitle
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Content Slot
                content()

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dismissText != null && onDismiss != null) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = dismissText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = isConfirmEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern notification permission rationale dialog.
 */
@Composable
fun NotificationPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModernModalDialog(
        onDismissRequest = onDismiss,
        title = "실시간 알림 켜기",
        subtitle = "Keep & Grow에서 목표 수익률 도달, 급등락 경보 및 포트폴리오 리밸런싱 알림을 받아보세요.",
        icon = Icons.Outlined.NotificationsActive,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmText = "알림 켜기",
        onConfirm = onConfirm,
        dismissText = "나중에",
        onDismiss = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "지원하는 알림 기능",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 보유 종목 및 관심 종목의 일일 급등락 알림\n• AI 진단 및 목표가 도달 시 스마트 알림\n• 오프라인 캐시 자동 동기화 완료 알림",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
