package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
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
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.HOME,
                        onClick = { viewModel.selectTab(ScreenTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("홈", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.STOCKS,
                        onClick = { viewModel.selectTab(ScreenTab.STOCKS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Screener"
                            )
                        },
                        label = { Text("종목", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PORTFOLIO,
                        onClick = { viewModel.selectTab(ScreenTab.PORTFOLIO) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Portfolio"
                            )
                        },
                        label = { Text("포트폴리오", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.HIGH_GROWTH,
                        onClick = { viewModel.selectTab(ScreenTab.HIGH_GROWTH) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "High Growth"
                            )
                        },
                        label = { Text("고성장 24", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
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

