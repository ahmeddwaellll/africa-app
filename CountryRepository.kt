package com.example.africanschools.data.repository

import com.example.africanschools.data.dao.CountryDao
import com.example.africanschools.data.model.Country
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CountryRepository @Inject constructor(private val countryDao: CountryDao) {
    fun getAllCountries(): Flow<List<Country>> {
        return countryDao.getAllCountries()
    }

    suspend fun getCountryById(countryId: String): Country? {
        return countryDao.getCountryById(countryId)
    }

    suspend fun insertCountries(countries: List<Country>) {
        countryDao.insertCountries(countries)
    }

    suspend fun updateCountry(country: Country) {
        countryDao.updateCountry(country)
    }

    suspend fun deleteCountry(country: Country) {
        countryDao.deleteCountry(country)
    }

    suspend fun deleteAllCountries() {
        countryDao.deleteAllCountries()
    }
}
