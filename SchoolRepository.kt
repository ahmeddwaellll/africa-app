package com.example.africanschools.data.repository

import com.example.africanschools.data.dao.SchoolDao
import com.example.africanschools.data.model.School
import com.example.africanschools.data.model.SchoolType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SchoolRepository @Inject constructor(
    private val schoolDao: SchoolDao,
    private val apiService: ApiService // Added ApiService for network calls
) {

    fun getSchoolsByProvince(provinceId: String): Flow<List<School>> {
        return schoolDao.getSchoolsByProvince(provinceId)
    }

    fun getFilteredSchools(provinceId: String, filters: SchoolFilters, schoolType: SchoolType): Flow<List<School>> {
        return schoolDao.getSchoolsByAcceptanceRateAndType(
            provinceId = provinceId,
            minAcceptanceRate = filters.minAcceptanceRate,
            maxAcceptanceRate = filters.maxAcceptanceRate,
            schoolType = schoolType
        ).combine(
            schoolDao.getSchoolsByFeesAndType(
                provinceId = provinceId,
                minFees = filters.minFees,
                maxFees = filters.maxFees,
                schoolType = schoolType
            )
        ) { acceptanceRateSchools, feeSchools ->
            acceptanceRateSchools.intersect(feeSchools).toList()
        }
    }

    suspend fun getSchoolById(schoolId: String): School? {
        // Fetch school details from the API if not found in the local database
        val schoolFromApi = apiService.getSchoolDetails(schoolId) 
        return schoolFromApi?.let { 
            schoolDao.insertSchools(listOf(it)) // Insert into local database
            it 
        } ?: schoolDao.getSchoolById(schoolId) 
    }

    suspend fun insertSchools(schools: List<School>) {
        schoolDao.insertSchools(schools)
    }

    suspend fun updateSchool(school: School) {
        schoolDao.updateSchool(school)
    }

    suspend fun deleteSchool(school: School) {
        schoolDao.deleteSchool(school)
    }

    suspend fun deleteSchoolsByProvince(provinceId: String) { 
        schoolDao.deleteSchoolsByProvince(provinceId) 
        // Optionally, add logic to delete from the API if needed
    }
}
