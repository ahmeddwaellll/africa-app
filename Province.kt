package com.example.africanschools.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "provinces",
    foreignKeys = [
        ForeignKey(
            entity = Country::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Province(
    @PrimaryKey val id: String,
    val countryId: String,
    val name: String,
    val type: ProvinceType, // PROVINCE or CITY
    val totalSchools: Int,
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class ProvinceType {
    PROVINCE,
    CITY
}
