package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "guest",
    val amount: Double,
    val forWhat: String,
    val paidTo: String,
    val category: String,
    val date: Long = System.currentTimeMillis()
)
