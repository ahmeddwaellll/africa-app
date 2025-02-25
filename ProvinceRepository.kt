package com.example.africanschools.data.repository

import com.example.africanschools.data.dao.ProvinceDao
import com.example.africanschools.data.model.Province
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProvinceRepository @Inject constructor(private val provinceDao: ProvinceDao) {
    fun getProvincesByCountry(countryId: String): Flow<List<Province>> {
        return provinceDao.getProvincesByCountry(countryId)
    }

    suspend fun getProvinceById(provinceId: String): Province? {
        return provinceDao.getProvinceById(provinceId)
    }

    suspend fun insertProvinces(provinces: List<Province>) {
        provinceDao.insertProvinces(provinces)
    }

    suspend fun updateProvince(province: Province) {
        provinceDao.updateProvince(province)
    }

    suspend fun deleteProvince(province: Province) {
        provinceDao.deleteProvince(province)
    }

    suspend fun deleteProvincesByCountry(countryId: String) {
        provinceDao.deleteProvincesByCountry(countryId)
    }
}
