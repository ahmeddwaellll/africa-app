package com.example.africanschools.data.dao

import androidx.room.*
import com.example.africanschools.data.model.Country
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun getAllCountries(): Flow<List<Country>>

    @Query("SELECT * FROM countries WHERE id = :countryId")
    suspend fun getCountryById(countryId: String): Country?

    @Query("SELECT * FROM countries WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchCountries(searchQuery: String): Flow<List<Country>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<Country>)

    @Update
    suspend fun updateCountry(country: Country)

    @Delete
    suspend fun deleteCountry(country: Country)

    @Query("DELETE FROM countries")
    suspend fun deleteAllCountries()
}
