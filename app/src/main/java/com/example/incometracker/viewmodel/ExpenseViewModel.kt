package com.example.incometracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.incometracker.data.ExpenseDao
import com.example.incometracker.data.ExpenseEntry
import com.example.incometracker.data.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseFormState(
    val nextSlNo: Long = 1,
    val purchaseMaterials: String = "",
    val quantity: String = "",
    val unit: String = "Nos",
    val vendorName: String = "",
    val totalAmount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val creditDeadlineDate: Long? = null,
    val isCreditError: Boolean = false,
    val isAmountError: Boolean = false
)

class ExpenseViewModel(private val dao: ExpenseDao) : ViewModel() {
    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState = _formState.asStateFlow()

    val allExpenses = dao.getAllExpenses().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        loadNextSlNo()
    }

    fun loadNextSlNo() {
        viewModelScope.launch {
            val count = dao.getTotalCount()
            _formState.value = _formState.value.copy(nextSlNo = (count + 1).toLong())
        }
    }

    fun updateMaterials(v: String) { _formState.value = _formState.value.copy(purchaseMaterials = v) }
    fun updateQuantity(v: String) { _formState.value = _formState.value.copy(quantity = v) }
    fun updateUnit(v: String) { _formState.value = _formState.value.copy(unit = v) }
    fun updateVendor(v: String) { _formState.value = _formState.value.copy(vendorName = v) }
    fun updateAmount(v: String) { _formState.value = _formState.value.copy(totalAmount = v, isAmountError = false) }

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
        val parsedAmount = state.totalAmount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0.0) {
            _formState.value = state.copy(isAmountError = true)
            return
        }

        if (state.paymentMethod == PaymentMethod.CREDIT && state.creditDeadlineDate == null) {
            _formState.value = state.copy(isCreditError = true)
            return
        }

        viewModelScope.launch {
            val entry = ExpenseEntry(
                date = System.currentTimeMillis(),
                purchaseMaterials = state.purchaseMaterials,
                quantity = state.quantity.toDoubleOrNull() ?: 1.0,
                unit = state.unit,
                vendorName = state.vendorName,
                totalAmount = parsedAmount,
                paymentMethod = state.paymentMethod,
                creditDeadlineDate = state.creditDeadlineDate
            )
            dao.insertExpense(entry)
            _formState.value = ExpenseFormState()
            loadNextSlNo()
            onSuccess()
        }
    }
}
