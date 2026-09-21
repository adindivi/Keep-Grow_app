package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.ConnectionMode
import com.example.ui.components.ModernInAppToast
import com.example.ui.components.NotificationPermissionRationaleDialog
import com.example.ui.screens.AssetSettingsDialog
import com.example.ui.screens.DiagnosisDetailsPanel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.ScreenerScreen
import com.example.ui.screens.HighGrowthScreenerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TossGray200
import com.example.ui.theme.TossGray600
import com.example.ui.theme.TossBlue50
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val isViewingDetail by viewModel.selectedStockForDetail.collectAsState()
    var showAssetSettings by remember { mutableStateOf(false) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var inAppToastMessage by remember { mutableStateOf<String?>(null) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showToast("실시간 알림이 성공적으로 활성화되었습니다! 🔔")
        } else {
            viewModel.showToast("알림 권한이 거부되었습니다. 설정에서 켤 수 있습니다.")
        }
    }

    // Real-time System Toast Listener
    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.toastMessage.collect { msg ->
            inAppToastMessage = msg
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Display clean header if not viewing stock detail
            if (isViewingDetail == null) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        val connectionMode by viewModel.connectionMode.collectAsState()
                        val pendingTasks by viewModel.pendingTasks.collectAsState()
                        val isRealConnected by viewModel.isRealDataConnected.collectAsState()

                        val (bgColor, dotColor, textColor, labelText, toastMsg) = when (connectionMode) {
                            ConnectionMode.ONLINE -> {
                                if (isRealConnected == true) {
                                    BadgeUi(
                                        Color(0xFFE8F5E9),
                                        Color(0xFF4CAF50),
                                        Color(0xFF2E7D32),
                                        "실제 연결됨",
                                        "실제 주식 데이터 서버와 실시간으로 연동되어 있습니다."
                                    )
                                } else {
                                    BadgeUi(
                                        Color(0xFFECEFF1),
                                        Color(0xFF90A4AE),
                                        Color(0xFF455A64),
                                        "연결 확인 중",
                                        "데이터 서버와 연결 상태를 확인하고 있습니다."
                                    )
                                }
                            }
                            ConnectionMode.SYNCING -> {
                                BadgeUi(
                                    Color(0xFFE3F2FD),
                                    Color(0xFF2196F3),
                                    Color(0xFF1565C0),
                                    "동기화 중...",
                                    "네트워크 복구 후 대기 중인 주가 및 환율 데이터를 동기화하고 있습니다."
                                )
                            }
                            ConnectionMode.OFFLINE -> {
                                val taskCount = pendingTasks.size
                                val text = if (taskCount > 0) "오프라인 (${taskCount}개 대기)" else "오프라인 (캐시)"
                                BadgeUi(
                                    Color(0xFFFFF3E0),
                                    Color(0xFFFF9800),
                                    Color(0xFFE65100),
                                    text,
                                    "인터넷 연결이 끊겨 로컬 캐시 데이터를 표시합니다. 재연결 시 자동으로 최신 주가를 동기화합니다."
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(bgColor)
                                .clickable {
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Text(
                                text = labelText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Keep ",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "& Grow",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val isGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (isGranted) {
                                    viewModel.showToast("실시간 주가 및 변동 알림이 켜져 있습니다. 🔔")
                                } else {
                                    showNotificationRationale = true
                                }
                            } else {
                                viewModel.showToast("실시간 주가 및 변동 알림이 켜져 있습니다. 🔔")
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable {
                                    showAssetSettings = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "WY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            // Show bottom navigation if not viewing stock detail
            if (isViewingDetail == null) {
                // ── Fintech-style Navigation Bar ──────────────────────────────
                // 순백(White) 배경 + 상단 1px TossGray200 구분선 + 미세 그림자
                Surface(
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = TossGray200,
                                shape = RoundedCornerShape(0.dp)
                            )
                    ) {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 0.dp
                        ) {
                            // ── 홈 탭 ──────────────────────────────────────
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.HOME,
                                onClick = { viewModel.selectTab(ScreenTab.HOME) },
                                icon = {
                                    // 활성: 화이트 캡슐 + 1px TossGray200 테두리 + 파란 아이콘
                                    // 비활성: 아이콘만 (TossGray600)
                                    if (currentTab == ScreenTab.HOME) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 32.dp)
                                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(100.dp), clip = false)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(TossBlue50)
                                                .border(1.dp, TossGray200, RoundedCornerShape(100.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Home,
                                                contentDescription = "Home",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Home,
                                            contentDescription = "Home",
                                            tint = TossGray600,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        "홈",
                                        fontWeight = if (currentTab == ScreenTab.HOME) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = TossGray600,
                                    indicatorColor = Color.Transparent
                                )
                            )

                            // ── 종목 탭 ────────────────────────────────────
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.STOCKS,
                                onClick = { viewModel.selectTab(ScreenTab.STOCKS) },
                                icon = {
                                    if (currentTab == ScreenTab.STOCKS) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 32.dp)
                                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(100.dp), clip = false)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(TossBlue50)
                                                .border(1.dp, TossGray200, RoundedCornerShape(100.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                contentDescription = "Screener",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                                            contentDescription = "Screener",
                                            tint = TossGray600,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        "종목",
                                        fontWeight = if (currentTab == ScreenTab.STOCKS) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = TossGray600,
                                    indicatorColor = Color.Transparent
                                )
                            )

                            // ── 포트폴리오 탭 ──────────────────────────────
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.PORTFOLIO,
                                onClick = { viewModel.selectTab(ScreenTab.PORTFOLIO) },
                                icon = {
                                    if (currentTab == ScreenTab.PORTFOLIO) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 32.dp)
                                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(100.dp), clip = false)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(TossBlue50)
                                                .border(1.dp, TossGray200, RoundedCornerShape(100.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Portfolio",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalanceWallet,
                                            contentDescription = "Portfolio",
                                            tint = TossGray600,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        "포트폴리오",
                                        fontWeight = if (currentTab == ScreenTab.PORTFOLIO) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = TossGray600,
                                    indicatorColor = Color.Transparent
                                )
                            )

                            // ── 고성장 24 탭 ───────────────────────────────
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.HIGH_GROWTH,
                                onClick = { viewModel.selectTab(ScreenTab.HIGH_GROWTH) },
                                icon = {
                                    if (currentTab == ScreenTab.HIGH_GROWTH) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 32.dp)
                                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(100.dp), clip = false)
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(TossBlue50)
                                                .border(1.dp, TossGray200, RoundedCornerShape(100.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.QueryStats,
                                                contentDescription = "High Growth",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.QueryStats,
                                            contentDescription = "High Growth",
                                            tint = TossGray600,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        "고성장24",
                                        fontWeight = if (currentTab == ScreenTab.HIGH_GROWTH) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = TossGray600,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                ScreenTab.STOCKS -> ScreenerScreen(viewModel = viewModel)
                ScreenTab.PORTFOLIO -> PortfolioScreen(viewModel = viewModel)
                ScreenTab.HIGH_GROWTH -> HighGrowthScreenerScreen()
            }

            // Slide up detail popup sheet
            DiagnosisDetailsPanel(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )

            if (showAssetSettings) {
                AssetSettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showAssetSettings = false }
                )
            }

            if (showNotificationRationale) {
                NotificationPermissionRationaleDialog(
                    onConfirm = {
                        showNotificationRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onDismiss = { showNotificationRationale = false }
                )
            }

            // Modern In-App Toast Notification Banner
            ModernInAppToast(
                message = inAppToastMessage,
                onDismiss = { inAppToastMessage = null },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private data class BadgeUi(
    val bgColor: Color,
    val dotColor: Color,
    val textColor: Color,
    val labelText: String,
    val toastMsg: String
)

