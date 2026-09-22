package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ── App Brand Colors ─────────────────────────────────────────────────────────
val Primary = Color(0xFF0058bc)
val PrimaryContainer = Color(0xFF0070eb)
val OnPrimary = Color(0xFFFFFFFF)

val Secondary = Color(0xFF4c4aca)
val OnSecondary = Color(0xFFFFFFFF)

val Tertiary = Color(0xFF8a2bb9)
val OnTertiary = Color(0xFFFFFFFF)

val Background = Color(0xFFF2F2F7) // Soft gray background for iOS/Premium-Minimal feel
val OnBackground = Color(0xFF1a1b1f)

val Surface = Color(0xFFFAF9FE)
val OnSurface = Color(0xFF1a1b1f)
val OnSurfaceVariant = Color(0xFF414755)

val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF4F3F8)
val SurfaceContainer = Color(0xFFEEEDF3)
val SurfaceContainerHigh = Color(0xFFE9E7ED)

val OutlinedBorder = Color(0xFFC1C6D7)

val PointError = Color(0xFFBA1A1A)
val PointErrorContainer = Color(0xFFFFDAD6)

// Dark Colors fallback (or generic)
val PrimaryDark = Color(0xFFADC6FF)
val BackgroundDark = Color(0xFF101114) // Slightly darker for deeper blacks & premium contrast
val SurfaceDark = Color(0xFF1A1B1F) // Distinct surface depth
val OnPrimaryDark = Color(0xFF00224D)
val OnBackgroundDark = Color(0xFFECEFF5)
val OnSurfaceDark = Color(0xFFECEFF5)
val OnSurfaceVariantDark = Color(0xFFC5C6D0)
val SurfaceVariantDark = Color(0xFF25262B)

// ── Toss Fintech Design System Palette ───────────────────────────────────────
// Gray Scale (토스 그레이 스케일)
val TossGray50  = Color(0xFFF9FAFB)   // 카드 배경
val TossGray100 = Color(0xFFF2F4F6)   // 비활성 배지 배경
val TossGray200 = Color(0xFFE5E8EB)   // 캡슐 테두리 1px (핵심 토큰!)
val TossGray300 = Color(0xFFD1D6DB)   // 구분선
val TossGray400 = Color(0xFFB0B8C1)   // Disabled 텍스트
val TossGray500 = Color(0xFF8B95A1)   // 서브 텍스트 (라이트)
val TossGray600 = Color(0xFF6B7684)   // 서브 텍스트 (다크)
val TossGray700 = Color(0xFF4E5968)   // 보조 라벨
val TossGray900 = Color(0xFF191F28)   // 최상위 제목

// Semantic Colors (핀테크 시맨틱 색상)
val TossBlue    = Color(0xFF1B64DA)   // 주요 액션 / 링크
val TossBlue50  = Color(0xFFEBF0FB)   // 파란 배지 배경
val TossGreen   = Color(0xFF00C073)   // 수익 / 긍정
val TossGreen50 = Color(0xFFE5F9F0)   // 초록 배지 배경
val TossRed     = Color(0xFFF04452)   // 손실 / 위험
val TossRed50   = Color(0xFFFEEEEF)   // 빨간 배지 배경
val TossOrange  = Color(0xFFFF8000)   // 경고
val TossOrange50= Color(0xFFFFF3E0)   // 주황 배지 배경

// Icon Badge (화이트 캡슐 배지 기준)
val FintechBadgeBackground = Color(0xFFFFFFFF)   // 순백 배경
val FintechBadgeBorder     = TossGray200          // 1px 테두리

// ── B2B Enterprise Pro Slate Navy & Terminal Palette ─────────────────────────
val SlateDeepNavy        = Color(0xFF0F172A)   // 터미널 딥 네이비 배경 (블룸버그 스타일)
val SlateSurface         = Color(0xFF1E293B)   // 카드 및 컨테이너 배경
val SlateSurfaceElevated = Color(0xFF334155)   // 상승 요소 및 탭 배경
val SlateBorder          = Color(0xFF334155)   // 1px 뮤티드 메탈릭 보더
val SlateBorderLight     = Color(0xFF475569)   // 포커스/강조 보더
val TerminalCyan         = Color(0xFF38BDF8)   // 프로페셔널 사이언 액센트
val TerminalEmerald      = Color(0xFF34D399)   // 퀀트 수익/초과달성 그린
val TerminalRose         = Color(0xFFFB7185)   // 퀀트 손실/리스크 로즈
val TerminalAmber        = Color(0xFFFBBF24)   // 주의/경고 앰버
val SlateTextPrimary     = Color(0xFFF8FAFC)   // 메인 텍스트 (순백 대비)
val SlateTextSecondary   = Color(0xFF94A3B8)   // 보조 텍스트 (슬레이트 그레이)
val SlateTextMuted       = Color(0xFF64748B)   // 비활성 텍스트

