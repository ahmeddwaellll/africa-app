package com.example.africanschools.data.dao

import androidx.room.*
import com.example.africanschools.data.model.School
import com.example.africanschools.data.model.SchoolType
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND acceptanceRate BETWEEN :min AND :max
    """)
    fun getSchoolsByAcceptanceRate(provinceId: String, min: Int, max: Int): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE provinceId = :provinceId ORDER BY name ASC")  
    fun getSchoolsByProvince(provinceId: String): Flow<List<School>>  

    @Query("""
        SELECT * FROM schools
        WHERE provinceId = :provinceId
        AND acceptanceRate BETWEEN :minAcceptanceRate AND :maxAcceptanceRate
        AND type = :schoolType
        ORDER BY name ASC
    """)
    fun getSchoolsByAcceptanceRateAndType(
        provinceId: String,
        minAcceptanceRate: Int,
        maxAcceptanceRate: Int,
        schoolType: SchoolType
    ): Flow<List<School>>

    @Query("""
        SELECT * FROM schools
        WHERE provinceId = :provinceId
        AND annualFees BETWEEN :minFees AND :maxFees
        AND type = :schoolType
        ORDER BY name ASC
    """)
    fun getSchoolsByFeesAndType(
        provinceId: String,
        minFees: Double,
        maxFees: Double,
        schoolType: SchoolType
    ): Flow<List<School>>

    @Query("""  
        SELECT * FROM schools  
        WHERE provinceId = :provinceId  
        AND annualFees BETWEEN :minFees AND :maxFees  
        ORDER BY name ASC  
    """)  
    fun getSchoolsByFees(  
        provinceId: String,  
        minFees: Double,  
        maxFees: Double  
    ): Flow<List<School>>  


    @Query("SELECT * FROM schools WHERE id = :schoolId")
    suspend fun getSchoolById(schoolId: String): School?

    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND facilities LIKE '%' || :facility || '%'
    """)
    fun getSchoolsByFacility(provinceId: String, facility: String): Flow<List<School>>

    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND programs LIKE '%' || :program || '%'
    """)
    fun getSchoolsByProgram(provinceId: String, program: String): Flow<List<School>>

    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND admissionRequirements LIKE '%' || :requirement || '%'
    """)
    fun getSchoolsByRequirement(provinceId: String, requirement: String): Flow<List<School>>


    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND type = :schoolType 
        ORDER BY name ASC
    """)
    fun getSchoolsByType(provinceId: String, schoolType: SchoolType): Flow<List<School>>

    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND (
            name LIKE '%' || :searchQuery || '%'
            OR description LIKE '%' || :searchQuery || '%'
            OR programs LIKE '%' || :searchQuery || '%'
            OR facilities LIKE '%' || :searchQuery || '%'
        )
    """)
    fun searchSchools(provinceId: String, searchQuery: String): Flow<List<School>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    @Update
    suspend fun updateSchool(school: School)

    @Delete
    suspend fun deleteSchool(school: School)

    @Query("DELETE FROM schools WHERE provinceId = :provinceId")
    suspend fun deleteSchoolsByProvince(provinceId: String)
}
