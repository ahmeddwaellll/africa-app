package com.example.africanschools.data.dao

import androidx.room.*
import com.example.africanschools.data.model.Province
import kotlinx.coroutines.flow.Flow

@Dao
interface ProvinceDao {
    @Query("SELECT * FROM provinces WHERE countryId = :countryId ORDER BY name ASC")
    fun getProvincesByCountry(countryId: String): Flow<List<Province>>

    @Query("SELECT * FROM provinces WHERE id = :provinceId")
    suspend fun getProvinceById(provinceId: String): Province?

    @Query("""
        SELECT * FROM provinces 
        WHERE countryId = :countryId 
        AND name LIKE '%' || :searchQuery || '%'
    """)
    fun searchProvinces(countryId: String, searchQuery: String): Flow<List<Province>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvinces(provinces: List<Province>)

    @Update
    suspend fun updateProvince(province: Province)

    @Delete
    suspend fun deleteProvince(province: Province)

    @Query("DELETE FROM provinces WHERE countryId = :countryId")
    suspend fun deleteProvincesByCountry(countryId: String)
}
