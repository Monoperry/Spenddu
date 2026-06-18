package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Expense
import com.example.data.ExpenseDatabase
import com.example.data.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    // PERSISTENT SETTINGS
    private val prefs = application.getSharedPreferences("spenddu_prefs", android.content.Context.MODE_PRIVATE)

    // Sign in state (local authentication state)
    private val _selectedCurrency = MutableStateFlow("$")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    val allExpenses: StateFlow<List<Expense>> 
    val monthlyReports: StateFlow<Map<String, List<Expense>>>

    private val _dailyCap = MutableStateFlow(0.0)
    val dailyCap: StateFlow<Double> = _dailyCap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Sheet state to control automatic opening at first launch
    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    init {
        val expenseDao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)

        loadUserSettings()

        allExpenses = repository.getAllExpensesForUser("guest").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        monthlyReports = allExpenses.map { list ->
            val grouped = LinkedHashMap<String, MutableList<Expense>>()
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            
            list.forEach { expense ->
                val monthStr = monthFormat.format(Date(expense.date))
                if (!grouped.containsKey(monthStr)) {
                    grouped[monthStr] = mutableListOf()
                }
                grouped[monthStr]?.add(expense)
            }

            grouped.mapValues { entry ->
                entry.value.sortedBy { it.date }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
    }

    private fun loadUserSettings() {
        _selectedCurrency.value = prefs.getString("guest_selected_currency", "$") ?: "$"
        _dailyCap.value = prefs.getFloat("guest_daily_cap", 0.0f).toDouble()
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString("guest_selected_currency", currency).apply()
        _selectedCurrency.value = currency
    }

    fun setDailyCap(cap: Double) {
        prefs.edit().putFloat("guest_daily_cap", cap.toFloat()).apply()
        _dailyCap.value = cap
    }

    fun addExpense(amount: Double, forWhat: String, paidTo: String, category: String, date: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(1200) // Fake loading state for polish
            repository.insert(Expense(userEmail = "guest", amount = amount, forWhat = forWhat, paidTo = paidTo, category = category, date = date))
            _isLoading.value = false
            onSuccess()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun markFirstLaunchComplete() {
        _isFirstLaunch.value = false
    }
}
