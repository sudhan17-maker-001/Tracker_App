package com.example.incometracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(entry: IncomeEntry): Long

    @Query("SELECT COUNT(*) FROM income_entries")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(DISTINCT customerId) FROM income_entries")
    suspend fun getDistinctCustomerCount(): Int

    @Query("SELECT * FROM income_entries ORDER BY slNo DESC")
    fun getAllIncomes(): Flow<List<IncomeEntry>>

    @Query("SELECT * FROM income_entries WHERE customerId = :custId ORDER BY date DESC")
    fun getIncomesByCustomer(custId: String): Flow<List<IncomeEntry>>

    @Query("SELECT DISTINCT customerId FROM income_entries ORDER BY customerId ASC")
    fun getAllCustomerIds(): Flow<List<String>>
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(entry: ExpenseEntry): Long

    @Query("SELECT COUNT(*) FROM expense_entries")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM expense_entries ORDER BY slNo DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntry>>
}
