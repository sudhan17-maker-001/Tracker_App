package com.example.incometracker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.example.incometracker.data.IncomeEntry
import com.example.incometracker.data.PaymentMethod
import com.example.incometracker.viewmodel.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IncomeScreen(viewModel: IncomeViewModel) {
    val state by viewModel.formState.collectAsState()
    val incomes by viewModel.allIncomes.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("New Income") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Grouped by Customer") })
        }

        if (selectedTab == 0) {
            // Entry Form
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
                        value = state.nextCustomerId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("2. Customer ID") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("3. Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.customerName,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("4. Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.customerMobile,
                    onValueChange = { viewModel.updateMobile(it) },
                    label = { Text("5. Customer Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.material,
                    onValueChange = { viewModel.updateMaterial(it) },
                    label = { Text("6. Material") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.item,
                    onValueChange = { viewModel.updateItem(it) },
                    label = { Text("7. Item") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Amount (₹)") },
                    isError = state.isAmountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8. Payment Method Dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = state.paymentMethod.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("8. Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name.replace("_", " ")) },
                                onClick = {
                                    viewModel.onPaymentMethodChanged(method)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 9. Conditional Credit Deadline Date
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
                                    "Deadline date is required for Credit payments",
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
                    Text("Submit Income Entry")
                }
            }
        } else {
            // Grouped By Customer View
            val groupedByCustomer = incomes.groupBy { it.customerId }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedByCustomer.forEach { (customerId, customerEntries) ->
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Customer Group: $customerId (${customerEntries.firstOrNull()?.customerName ?: ""})",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(customerEntries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Sl. No: #${entry.slNo} | Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.date))}")
                                Text("Item: ${entry.item} (${entry.material})")
                                Text("Payment: ${entry.paymentMethod} | ₹${entry.amount}", fontWeight = FontWeight.SemiBold)
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
