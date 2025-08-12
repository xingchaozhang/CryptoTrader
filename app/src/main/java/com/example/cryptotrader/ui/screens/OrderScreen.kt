package com.example.cryptotrader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotrader.data.OrderSide
import com.example.cryptotrader.data.OrderType
import com.example.cryptotrader.ui.OrderViewModel

/**
 * Screen for entering an order for the selected crypto pair.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController, symbol: String) {
    val viewModel: OrderViewModel = hiltViewModel()
    val price by viewModel.price.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val side by viewModel.side.collectAsState()
    val type by viewModel.type.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var sideExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order $symbol") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = if (price == 0.0) "" else price.toString(),
                onValueChange = { viewModel.setPrice(it) },
                label = { Text("Price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if (amount == 0.0) "" else amount.toString(),
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            // Side dropdown
            ExposedDropdownMenuBox(expanded = sideExpanded, onExpandedChange = { sideExpanded = !sideExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = side.name,
                    onValueChange = {},
                    label = { Text("Side") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sideExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = sideExpanded,
                    onDismissRequest = { sideExpanded = false }
                ) {
                    OrderSide.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                viewModel.setSide(option)
                                sideExpanded = false
                            }
                        )
                    }
                }
            }

            // Type dropdown
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = type.name,
                    onValueChange = {},
                    label = { Text("Order Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    OrderType.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                viewModel.setType(option)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                viewModel.confirmOrder()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Confirm Order")
            }
            if (statusMessage.isNotBlank()) {
                Text(statusMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
