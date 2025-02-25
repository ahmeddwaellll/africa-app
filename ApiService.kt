package com.example.africanschools.network

import com.example.africanschools.data.model.*
import retrofit2.http.*

interface ApiService {
    @GET("countries")
    suspend fun getCountries(
        @Query("language") language: String? = null
    ): List<ApiCountry>

    @GET("countries/{countryId}")
    suspend fun getCountryDetails(
        @Path("countryId") countryId: String,
        @Query("language") language: String? = null
    ): ApiCountryDetails

    @GET("countries/{countryId}/provinces")
    suspend fun getProvinces(
        @Path("countryId") countryId: String,
        @Query("language") language: String? = null
    ): List<ApiProvince>

    @GET("provinces/{provinceId}/schools")
    suspend fun getSchools(
        @Path("provinceId") provinceId: String,
        @Header("Authorization") token: String,
        @Query("type") schoolType: SchoolType? = null,
        @Query("language") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiPaginatedResponse<ApiSchool>

    @GET("schools/search")
    suspend fun searchSchools(
        @Query("query") query: String,
        @Query("countryId") countryId: String? = null,
        @Query("provinceId") provinceId: String? = null,
        @Query("type") schoolType: SchoolType? = null,
        @Query("language") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiPaginatedResponse<ApiSchool>

    @GET("schools/{schoolId}")
    suspend fun getSchoolDetails(
        @Path("schoolId") schoolId: String,
        @Query("language") language: String? = null
    ): ApiSchoolDetails
}
