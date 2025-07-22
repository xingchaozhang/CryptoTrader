package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.cryptotrader.data.defaultFakeTickers

 * 保存后回调 onSave，并由外层切回查看模式。
 */
@Composable
fun FavoritesEditScreen(
    modifier: Modifier = Modifier,
    onSave: (Set<String>) -> Unit
) {
    val allPairs = remember { defaultFakeTickers().map { it.symbol } }
    val selected = remember { FavoritesRepository.symbols.value.toMutableStateList() }

    Box(modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)   
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allPairs.subList(0, 6)) { symbol ->
                    val checked = symbol in selected
                    FavoritePairCard(symbol, checked) {
                        if (checked) selected.remove(symbol) else selected.add(symbol)
                    }
                }
            }
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
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) { Text("加入自选") }
    }
}

@Composable
private fun FavoritePairCard(text: String, checked: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onToggle() }
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (checked) Color.Black else Color.Transparent,
                border = if (checked) null else BorderStroke(1.dp, Color.Black),
                modifier = Modifier.size(22.dp)
            ) { if (checked) Icon(Icons.Default.Check, null, tint = Color.White) }
        }
    }
}
