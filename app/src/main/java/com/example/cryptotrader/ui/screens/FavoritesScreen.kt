package com.example.cryptotrader.ui.screens

/* ---------------- 自选页 ---------------- */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cryptotrader.data.defaultFakeTickers

@Composable
@Preview
fun FavoritesScreen() {
    val allPairs      = remember { defaultFakeTickers().take(6).map { it.symbol } }
    val selectedPairs = remember { mutableStateListOf<String>() }

    Scaffold(
        bottomBar = {
            Button(
                onClick  = { /* TODO: persist */ },
                enabled  = selectedPairs.isNotEmpty(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor   = Color.White,
                    disabledContainerColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text("加入自选") }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            LazyVerticalGrid(
                columns           = GridCells.Fixed(2),
                modifier          = Modifier
                    .weight(1f)            // 关键：给底栏留空间
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding    = PaddingValues(bottom = 88.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allPairs) { pair ->
                    val checked = pair in selectedPairs
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clickable {
                                if (checked) selectedPairs.remove(pair)
                                else          selectedPairs.add(pair)
                            }
                    ) {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(pair, style = MaterialTheme.typography.bodyLarge)
                            Surface(
                                shape  = RoundedCornerShape(4.dp),
                                color  = if (checked) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                border = if (checked) null
                                else BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.size(22.dp)
                            ) {
                                if (checked) Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
