package com.example.africanschools.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class Country(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val flagUrl: String,
    val totalSchools: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
