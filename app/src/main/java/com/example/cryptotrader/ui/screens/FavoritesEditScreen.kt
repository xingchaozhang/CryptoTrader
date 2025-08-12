// FavoritesEditScreen.kt
package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cryptotrader.data.FavoritesRepository

/**
 * 编辑自选。保存后回调 onSave（外层切回查看模式）。
 */
@Composable
fun FavoritesEditScreen(
    modifier: Modifier = Modifier,
    onSave: (Set<String>) -> Unit,
    onCancel: () -> Unit
) {
    // 只提供 4 个币对
    val allPairs = remember { listOf("BTC/USDT", "ETH/USDT", "BNB/USDT", "SOL/USDT") }
    val selected = remember { FavoritesRepository.symbols.value.toMutableStateList() }

    // 外层底部导航的大致高度（让按钮不会被遮住）
    val outerBottomBarHeight = 72.dp
    // 自己这个按钮的高度 + 外边距，用于给网格腾出空间
    val ctaHeightWithMargin = 56.dp + 16.dp + outerBottomBarHeight

    Box(modifier = modifier.fillMaxSize()) {

        // 网格内容（为底部按钮和外层 bottom bar 让出空间）
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = ctaHeightWithMargin
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allPairs) { symbol ->
                val checked = symbol in selected
                FavoritePairCard(symbol, checked) {
                    if (checked) selected.remove(symbol) else selected.add(symbol)
                }
            }
        }

        // 底部按钮（不会被外层导航栏挡住）
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding() // 避开系统手势栏
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = outerBottomBarHeight) // 让出外层导航栏高度
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = { onSave(selected.toSet()) },
                    enabled = selected.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("加入自选")
                }
            }
        }
    }
}

@Composable
private fun FavoritePairCard(text: String, checked: Boolean, onToggle: () -> Unit) {
    // 卡片的视觉风格更贴近你截图（淡紫色底、圆角、轻浮起）
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clickable { onToggle() }
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (checked) Color.Black else Color.Transparent,
                border = if (checked) null else BorderStroke(1.dp, Color.Black),
                modifier = Modifier.size(24.dp)
            ) {
                if (checked) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}