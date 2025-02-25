package com.example.africanschools.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.africanschools.data.model.converters.Converters

@Entity(tableName = "schools")
@TypeConverters(Converters::class)
data class School(
    @PrimaryKey val id: String,
    val provinceId: String,
    val name: String,
    val establishedYear: Int,
    val type: SchoolType,
    val description: String,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val logoUrl: String,
    val cvUrl: String? = null,
    val applicationStatus: String? = null,
    val acceptanceRate: Int,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val imageUrls: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val programs: List<String> = emptyList(),
    val admissionRequirements: List<String> = emptyList(),
    val applicationDeadlines: Map<String, Long> = emptyMap(),
    val academicCalendar: Map<String, String> = emptyMap()
)
