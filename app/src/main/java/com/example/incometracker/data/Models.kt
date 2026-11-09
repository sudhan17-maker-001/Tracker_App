package com.example.incometracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH, UPI, CREDIT, OTHER_PAYMENTS
}

@Entity(tableName = "income_entries")
data class IncomeEntry(
    @PrimaryKey(autoGenerate = true)
    val slNo: Long = 0,
    val customerId: String,
    val date: Long,
    val customerName: String,
    val customerMobile: String,
    val material: String,
    val item: String,
    val paymentMethod: PaymentMethod,
    val creditDeadlineDate: Long?,
    val amount: Double
)

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val slNo: Long = 0,
    val date: Long,
    val purchaseMaterials: String,
    val quantity: Double,
    val unit: String,
    val vendorName: String,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val creditDeadlineDate: Long?
)
