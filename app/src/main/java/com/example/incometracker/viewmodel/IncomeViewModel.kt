package com.example.incometracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.incometracker.data.IncomeDao
import com.example.incometracker.data.IncomeEntry
import com.example.incometracker.data.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IncomeFormState(
    val nextSlNo: Long = 1,
    val nextCustomerId: String = "CUS-001",
    val customerName: String = "",
    val customerMobile: String = "",
    val material: String = "",
    val item: String = "",
    val amount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val creditDeadlineDate: Long? = null,
    val isCreditError: Boolean = false,
    val isAmountError: Boolean = false
)

class IncomeViewModel(private val dao: IncomeDao) : ViewModel() {
    private val _formState = MutableStateFlow(IncomeFormState())
    val formState = _formState.asStateFlow()

    val allIncomes = dao.getAllIncomes().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        loadNextIdentifiers()
    }

    fun loadNextIdentifiers() {
        viewModelScope.launch {
            val totalEntries = dao.getTotalCount()
            val distinctCustCount = dao.getDistinctCustomerCount()
            _formState.value = _formState.value.copy(
                nextSlNo = (totalEntries + 1).toLong(),
                nextCustomerId = String.format("CUS-%03d", distinctCustCount + 1)
            )
        }
    }

    fun updateName(v: String) { _formState.value = _formState.value.copy(customerName = v) }
    fun updateMobile(v: String) { _formState.value = _formState.value.copy(customerMobile = v) }
    fun updateMaterial(v: String) { _formState.value = _formState.value.copy(material = v) }
    fun updateItem(v: String) { _formState.value = _formState.value.copy(item = v) }
    fun updateAmount(v: String) { _formState.value = _formState.value.copy(amount = v, isAmountError = false) }

    fun onPaymentMethodChanged(method: PaymentMethod) {
        _formState.value = _formState.value.copy(
            paymentMethod = method,
            creditDeadlineDate = if (method != PaymentMethod.CREDIT) null else _formState.value.creditDeadlineDate,
            isCreditError = false
        )
    }

    fun onDeadlineSelected(timestamp: Long) {
        _formState.value = _formState.value.copy(creditDeadlineDate = timestamp, isCreditError = false)
    }

    fun submitEntry(onSuccess: () -> Unit) {
        val state = _formState.value
        val parsedAmount = state.amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0.0) {
            _formState.value = state.copy(isAmountError = true)
            return
        }

        if (state.paymentMethod == PaymentMethod.CREDIT && state.creditDeadlineDate == null) {
            _formState.value = state.copy(isCreditError = true)
            return
        }

        viewModelScope.launch {
            val entry = IncomeEntry(
                customerId = state.nextCustomerId,
                date = System.currentTimeMillis(),
                customerName = state.customerName,
                customerMobile = state.customerMobile,
                material = state.material,
                item = state.item,
                paymentMethod = state.paymentMethod,
                creditDeadlineDate = state.creditDeadlineDate,
                amount = parsedAmount
            )
            dao.insertIncome(entry)
            _formState.value = IncomeFormState()
            loadNextIdentifiers()
            onSuccess()
        }
    }
}
