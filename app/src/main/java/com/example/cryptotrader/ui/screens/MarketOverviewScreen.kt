package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.ui.MarketOverviewViewModel
import com.example.cryptotrader.ui.WatchlistViewModel

/**
 * Screen displaying the user's watchlist along with live price updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketOverviewScreen(
    navController: NavController,
    overviewViewModel: MarketOverviewViewModel = hiltViewModel(),
    watchlistViewModel: WatchlistViewModel = hiltViewModel()
) {
    val pairsWithPrices by overviewViewModel.pairsWithPrices.collectAsState()
    var newSymbol by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Overview") },
                actions = {
                    IconButton(onClick = { navController.navigate("orders") }) {
                        Icon(Icons.Default.List, contentDescription = "Orders")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Input to add new pair
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newSymbol,
                    onValueChange = { newSymbol = it },
                    label = { Text("Add Pair (e.g., BTCUSDT)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newSymbol.isNotBlank()) {
                            watchlistViewModel.addPair(newSymbol.trim().uppercase(), newSymbol.trim().uppercase())
                            newSymbol = ""
                        }
                    })
                )
                Button(onClick = {
                    if (newSymbol.isNotBlank()) {
                        watchlistViewModel.addPair(newSymbol.trim().uppercase(), newSymbol.trim().uppercase())
                        newSymbol = ""
                    }
                }) {
                    Text("Add")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pairsWithPrices.isEmpty()) {
                Text("No pairs in watchlist. Add a pair using the field above.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pairsWithPrices) { item ->
                        ListItem(
                            headlineContent = {
                                Text(item.entity.symbol)
                            },
                            supportingContent = {
                                val price = item.priceUpdate
                                Text(
                                    if (price != null) "Last: ${String.format("%.2f", price.last)} | Bid: ${String.format("%.2f", price.bid)} | Ask: ${String.format("%.2f", price.ask)}" else "Loading..."
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { watchlistViewModel.removePair(item.entity) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("detail/${item.entity.symbol}") }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}
