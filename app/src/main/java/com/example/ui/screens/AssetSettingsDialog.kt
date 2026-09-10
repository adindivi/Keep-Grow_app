package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userCash by viewModel.userCash.collectAsState()
    val portfolioStocks by viewModel.portfolioStocks.collectAsState()

    var cashInput by remember { mutableStateOf(userCash.toLong().toString()) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Track modifications to stock values locally
    val modifiedPrices = remember { mutableStateMapOf<Int, String>() }
    val modifiedQuantities = remember { mutableStateMapOf<Int, String>() }

    val formatter = remember { DecimalFormat("#,###") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .imePadding()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "쉬운 자산 및 투자금 설정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Middle Schooler Friendly Guidance Alert
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "내가 가진 현금과 주식을 입력해 두면, 인공지능(AI) 비서가 똑똑하게 분석해서 조언을 해줘요!",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 1: Cash Card UI
                    Text(
                        text = "💵 내 지갑 속 현금 (원화)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                cashInput = input
                            }
                        },
                        placeholder = { Text("가진 현금을 원화 단위로 써주세요", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    
                    if (cashInput.isNotEmpty()) {
                        val parsed = cashInput.toLongOrNull() ?: 0L
                        Text(
                            text = "👉 한눈에 보기: ${formatter.format(parsed)}원",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 2: Stock Holdings Card UI
                    Text(
                        text = "📈 내가 산 주식 카드들 (${portfolioStocks.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (portfolioStocks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 주식이 없어요!\n주식 상세 화면에서 관심등록 후 여기에 등록해 보세요.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        portfolioStocks.forEach { stock ->
                            // Read current or modified values
                            val currentPrice = modifiedPrices[stock.id] ?: stock.price.toInt().toString()
                            val currentQty = modifiedQuantities[stock.id] ?: stock.quantity.toString()

                            // Middle school friendly name mapping & Emojis
                            val emoji = getStockEmoji(stock.ticker, stock.name)
                            val tagline = when (stock.ticker.uppercase()) {
                                "005930" -> "삼성전자 - 갤럭시 스마트폰과 가전 대표 브랜드"
                                "000660" -> "SK하이닉스 - 고성능 AI 메모리 반도체 대표 제품"
                                "NVDA" -> "엔비디아 - 인공지능용 최고 스펙 그래픽카드(GPU)"
                                "AAPL" -> "애플 - 전세계가 사랑하는 아이폰과 맥북"
                                "TSLA" -> "테슬라 - 미래 혁신을 그리는 대표 전기자동차"
                                else -> "내가 아주 눈여겨보고 있는 특별한 기업"
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Stock Header Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = emoji,
                                                fontSize = 20.sp
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stock.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = tagline,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.removeStock(stock.id)
                                                Toast.makeText(context, "${stock.name} 삭제 완료", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Inputs Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Average Price TextField
                                        OutlinedTextField(
                                            value = currentPrice,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() }) {
                                                    modifiedPrices[stock.id] = input
                                                }
                                            },
                                            label = { Text("평단가 (원/달러)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = Color.Black,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                cursorColor = Color.Black,
                                                focusedContainerColor = Color(0xFFF8F9FA),
                                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )

                                        // Quantity TextField
                                        OutlinedTextField(
                                            value = currentQty,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() }) {
                                                    modifiedQuantities[stock.id] = input
                                                }
                                            },
                                            label = { Text("수량 (개)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(0.9f),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = Color.Black,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                cursorColor = Color.Black,
                                                focusedContainerColor = Color(0xFFF8F9FA),
                                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("취소할래", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true

                            // 1. Save user cash
                            val cashParsed = cashInput.toDoubleOrNull() ?: 0.0
                            viewModel.saveUserCash(cashParsed)

                            // 2. Save modified stock prices and quantities
                            portfolioStocks.forEach { stock ->
                                val priceVal = modifiedPrices[stock.id]?.toDoubleOrNull() ?: stock.price
                                val qtyVal = modifiedQuantities[stock.id]?.toIntOrNull() ?: stock.quantity
                                viewModel.updateManualStock(stock.id, priceVal, qtyVal)
                            }

                            Toast.makeText(context, "자산이 안전하게 저장되었어요! 💾", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("저장할래!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
