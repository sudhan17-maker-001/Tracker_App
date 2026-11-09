package com.example.incometracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.PaymentMethod
import com.example.incometracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val state by viewModel.formState.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedTab by remember { mutableStateOf(0) }

    val standardUnits = listOf("Nos", "Kg", "Bags", "Litres", "Tons", "Boxes", "Meters", "Sq.Ft")
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("New Expense") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Expense History") })
        }

        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "#${state.nextSlNo}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("1. Sl. No.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("2. Date") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = state.purchaseMaterials,
                    onValueChange = { viewModel.updateMaterials(it) },
                    label = { Text("3. Purchase Materials") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = { viewModel.updateQuantity(it) },
                        label = { Text("4. Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("5. Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            standardUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        viewModel.updateUnit(u)
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = state.vendorName,
                    onValueChange = { viewModel.updateVendor(it) },
                    label = { Text("6. Vendor Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.totalAmount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("7. Total Amount (₹)") },
                    isError = state.isAmountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = paymentDropdownExpanded,
                    onExpandedChange = { paymentDropdownExpanded = !paymentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = state.paymentMethod.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("8. Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentDropdownExpanded,
                        onDismissRequest = { paymentDropdownExpanded = false }
                    ) {
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name.replace("_", " ")) },
                                onClick = {
                                    viewModel.onPaymentMethodChanged(method)
                                    paymentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (state.paymentMethod == PaymentMethod.CREDIT) {
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Credit Term Details", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { showDatePicker = true }) {
                                Text(
                                    state.creditDeadlineDate?.let {
                                        "Deadline: " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "9. Select Credit Deadline Date *"
                                )
                            }
                            if (state.isCreditError) {
                                Text(
                                    "Deadline date is mandatory for Credit option",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.submitEntry {} },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Expense Entry")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { entry ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sl. No: #${entry.slNo} | Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.date))}")
                            Text("Material: ${entry.purchaseMaterials} (${entry.quantity} ${entry.unit})")
                            Text("Vendor: ${entry.vendorName}")
                            Text("Payment: ${entry.paymentMethod} | Total: ₹${entry.totalAmount}", fontWeight = FontWeight.SemiBold)
                            if (entry.paymentMethod == PaymentMethod.CREDIT && entry.creditDeadlineDate != null) {
                                Text(
                                    "Deadline: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.creditDeadlineDate))}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDeadlineSelected(it) }
                    showDatePicker = false
                }) { Text("Confirm") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
