// build.gradle (Project level)
buildscript {
    ext {
        kotlin_version = '1.8.0'
        compose_version = '1.4.0'
        room_version = '2.5.0'
        retrofit_version = '2.9.0'
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

// build.gradle (App level)
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
}

android {
    namespace 'com.example.africanschools'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.africanschools"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
}

dependencies {
    // Core Android dependencies
    implementation 'androidx.core:core-ktx:1.10.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    
    // Compose dependencies
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material:material:$compose_version"
    implementation "androidx.compose.ui:ui-tooling-preview:$compose_version"
    
    // Architecture Components
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1"
    implementation "androidx.navigation:navigation-compose:2.6.0"
    
    // Room Database
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
    
    // Retrofit for network calls
    implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
    implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
}
// app/src/main/java/com/example/africanschools/data/model/Country.kt
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

// app/src/main/java/com/example/africanschools/data/model/Province.kt
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

// app/src/main/java/com/example/africanschools/data/model/School.kt
package com.example.africanschools.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "schools",
    foreignKeys = [
        ForeignKey(
            entity = Province::class,
            parentColumns = ["id"],
            childColumns = ["provinceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    val imageUrls: List<String>,
    val facilities: List<String>,
    val programs: List<String>,
    val admissionRequirements: List<String>,
    val applicationDeadlines: Map<String, Long>, // Term to deadline timestamp
    val academicCalendar: Map<String, String>, // Term to date range
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class SchoolType {
    PRIMARY,
    SECONDARY,
    UNIVERSITY,
    TECHNICAL,
    VOCATIONAL
}

// app/src/main/java/com/example/africanschools/data/model/converters/Converters.kt
package com.example.africanschools.data.model.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromLongMap(value: Map<String, Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongMap(value: String): Map<String, Long> {
        val mapType = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
// app/src/main/java/com/example/africanschools/data/dao/CountryDao.kt
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

// app/src/main/java/com/example/africanschools/data/dao/ProvinceDao.kt
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

// app/src/main/java/com/example/africanschools/data/dao/SchoolDao.kt
package com.example.africanschools.data.dao

import androidx.room.*
import com.example.africanschools.data.model.School
import com.example.africanschools.data.model.SchoolType
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools WHERE provinceId = :provinceId ORDER BY name ASC")
    fun getSchoolsByProvince(provinceId: String): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE id = :schoolId")
    suspend fun getSchoolById(schoolId: String): School?

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
        )
    """)
    fun searchSchools(provinceId: String, searchQuery: String): Flow<List<School>>

    @Query("""
        SELECT * FROM schools 
        WHERE provinceId = :provinceId 
        AND type = :schoolType 
        AND (
            name LIKE '%' || :searchQuery || '%'
            OR description LIKE '%' || :searchQuery || '%'
        )
    """)
    fun searchSchoolsByType(
        provinceId: String,
        schoolType: SchoolType,
        searchQuery: String
    ): Flow<List<School>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    @Update
    suspend fun updateSchool(school: School)

    @Delete
    suspend fun deleteSchool(school: School)

    @Query("DELETE FROM schools WHERE provinceId = :provinceId")
    suspend fun deleteSchoolsByProvince(provinceId: String)
}

// app/src/main/java/com/example/africanschools/data/relations/ProvinceWithSchools.kt
package com.example.africanschools.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.africanschools.data.model.Province
import com.example.africanschools.data.model.School

data class ProvinceWithSchools(
    @Embedded val province: Province,
    @Relation(
        parentColumn = "id",
        entityColumn = "provinceId"
    )
    val schools: List<School>
)

// app/src/main/java/com/example/africanschools/data/relations/CountryWithProvinces.kt
package com.example.africanschools.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.africanschools.data.model.Country
import com.example.africanschools.data.model.Province

data class CountryWithProvinces(
    @Embedded val country: Country,
    @Relation(
        parentColumn = "id",
        entityColumn = "countryId"
    )
    val provinces: List<Province>
)
// app/src/main/java/com/example/africanschools/data/database/AfricanSchoolsDatabase.kt
package com.example.africanschools.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.africanschools.data.dao.CountryDao
import com.example.africanschools.data.dao.ProvinceDao
import com.example.africanschools.data.dao.SchoolDao
import com.example.africanschools.data.model.Country
import com.example.africanschools.data.model.Province
import com.example.africanschools.data.model.School
import com.example.africanschools.data.model.converters.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Country::class,
        Province::class,
        School::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AfricanSchoolsDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun provinceDao(): ProvinceDao
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AfricanSchoolsDatabase? = null
        private const val DATABASE_NAME = "african_schools.db"

        fun getInstance(context: Context): AfricanSchoolsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AfricanSchoolsDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AfricanSchoolsDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with initial data if needed
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prePopulateDatabase(database)
                            }
                        }
                    }
                })
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .build()
        }

        private suspend fun prePopulateDatabase(database: AfricanSchoolsDatabase) {
            // Add some initial countries if desired
            val initialCountries = listOf(
                Country(
                    id = "za",
                    name = "South Africa",
                    code = "ZA",
                    flagUrl = "https://example.com/flags/za.png",
                    totalSchools = 0
                ),
                Country(
                    id = "ng",
                    name = "Nigeria",
                    code = "NG",
                    flagUrl = "https://example.com/flags/ng.png",
                    totalSchools = 0
                )
                // Add more countries as needed
            )
            database.countryDao().insertCountries(initialCountries)
        }
    }
}

// app/src/main/java/com/example/africanschools/data/database/DatabaseMigrations.kt
package com.example.africanschools.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    // Example migration from version 1 to 2 (for future use)
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migration SQL statements here when needed
            // Example:
            // database.execSQL("ALTER TABLE schools ADD COLUMN rating FLOAT DEFAULT 0.0")
        }
    }

    // Add more migrations as needed
    val ALL_MIGRATIONS = arrayOf<Migration>(
        // Add migrations here when needed
        // MIGRATION_1_2
    )
}

// app/src/main/java/com/example/africanschools/di/DatabaseModule.kt
package com.example.africanschools.di

import android.content.Context
import com.example.africanschools.data.database.AfricanSchoolsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AfricanSchoolsDatabase {
        return AfricanSchoolsDatabase.getInstance(context)
    }

    @Provides
    fun provideCountryDao(database: AfricanSchoolsDatabase) = database.countryDao()

    @Provides
    fun provideProvinceDao(database: AfricanSchoolsDatabase) = database.provinceDao()

    @Provides
    fun provideSchoolDao(database: AfricanSchoolsDatabase) = database.schoolDao()
}
// app/src/main/java/com/example/africanschools/data/model/Language.kt
package com.example.africanschools.data.model

enum class AfricanLanguage {
    AMHARIC,      // Ethiopia
    ARABIC,       // North Africa
    SWAHILI,      // East Africa
    YORUBA,       // Nigeria
    IGBO,         // Nigeria
    HAUSA,        // Nigeria, Niger
    ZULU,         // South Africa
    XHOSA,        // South Africa
    AFRIKAANS,    // South Africa
    SESOTHO,      // South Africa, Lesotho
    SETSWANA,     // Botswana, South Africa
    LINGALA,      // DRC, Congo
    KINYARWANDA,  // Rwanda
    WOLOF,        // Senegal
    SOMALI,       // Somalia
    OROMO,        // Ethiopia
    MALAGASY,     // Madagascar
    BAMBARA,      // Mali
    FULFULDE,     // West Africa
    TIGRINYA      // Eritrea, Ethiopia
}

// app/src/main/java/com/example/africanschools/data/model/Translation.kt
package com.example.africanschools.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "translations",
    primaryKeys = ["entityId", "entityType", "language"],
    foreignKeys = [
        ForeignKey(
            entity = Country::class,
            parentColumns = ["id"],
            childColumns = ["entityId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Translation(
    val entityId: String,
    val entityType: EntityType,
    val language: AfricanLanguage,
    val name: String,
    val description: String? = null,
    val additionalInfo: Map<String, String> = emptyMap()
)

enum class EntityType {
    COUNTRY,
    PROVINCE,
    SCHOOL
}

// Update Country.kt
@Entity(tableName = "countries")
data class Country(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val flagUrl: String,
    val totalSchools: Int,
    val primaryLanguage: AfricanLanguage,
    val supportedLanguages: List<AfricanLanguage>,
    val lastUpdated: Long = System.currentTimeMillis()
)

// app/src/main/java/com/example/africanschools/data/dao/TranslationDao.kt
package com.example.africanschools.data.dao

import androidx.room.*
import com.example.africanschools.data.model.AfricanLanguage
import com.example.africanschools.data.model.EntityType
import com.example.africanschools.data.model.Translation
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("""
        SELECT * FROM translations 
        WHERE entityId = :entityId 
        AND entityType = :entityType 
        AND language = :language
    """)
    suspend fun getTranslation(
        entityId: String,
        entityType: EntityType,
        language: AfricanLanguage
    ): Translation?

    @Query("""
        SELECT * FROM translations 
        WHERE entityType = :entityType 
        AND language = :language
        AND name LIKE '%' || :searchQuery || '%'
    """)
    fun searchTranslations(
        entityType: EntityType,
        language: AfricanLanguage,
        searchQuery: String
    ): Flow<List<Translation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<Translation>)

    @Delete
    suspend fun deleteTranslation(translation: Translation)
}

// Update AfricanSchoolsDatabase.kt
@Database(
    entities = [
        Country::class,
        Province::class,
        School::class,
        Translation::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AfricanSchoolsDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
    // ... existing DAOs ...

    companion object {
        private suspend fun prePopulateDatabase(database: AfricanSchoolsDatabase) {
            // Example of multi-language country data
            val southAfrica = Country(
                id = "za",
                name = "South Africa",
                code = "ZA",
                flagUrl = "https://example.com/flags/za.png",
                totalSchools = 0,
                primaryLanguage = AfricanLanguage.ENGLISH,
                supportedLanguages = listOf(
                    AfricanLanguage.ZULU,
                    AfricanLanguage.XHOSA,
                    AfricanLanguage.AFRIKAANS,
                    AfricanLanguage.SESOTHO,
                    AfricanLanguage.SETSWANA
                )
            )

            // Example translations for South Africa
            val saTranslations = listOf(
                Translation(
                    entityId = "za",
                    entityType = EntityType.COUNTRY,
                    language = AfricanLanguage.ZULU,
                    name = "iNingizimu Afrika",
                    description = "INingizimu Afrika iyizwe elikhulu"
                ),
                Translation(
                    entityId = "za",
                    entityType = EntityType.COUNTRY,
                    language = AfricanLanguage.XHOSA,
                    name = "uMzantsi Afrika",
                    description = "UMzantsi Afrika lilizwe elikhulu"
                ),
                Translation(
                    entityId = "za",
                    entityType = EntityType.COUNTRY,
                    language = AfricanLanguage.AFRIKAANS,
                    name = "Suid-Afrika",
                    description = "Suid-Afrika is 'n groot land"
                )
            )

            database.countryDao().insertCountries(listOf(southAfrica))
            database.translationDao().insertTranslations(saTranslations)
        }
    }
}
// app/src/main/java/com/example/africanschools/data/repository/DatabaseRepository.kt
package com.example.africanschools.data.repository

import com.example.africanschools.data.database.AfricanSchoolsDatabase
import com.example.africanschools.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    private val database: AfricanSchoolsDatabase
) {
    private val countryDao = database.countryDao()
    private val provinceDao = database.provinceDao()
    private val schoolDao = database.schoolDao()
    private val translationDao = database.translationDao()

    // Language preference handling
    private var currentLanguage: AfricanLanguage = AfricanLanguage.ENGLISH

    fun setLanguage(language: AfricanLanguage) {
        currentLanguage = language
    }

    // Country Operations
    fun getCountriesWithTranslations(): Flow<List<CountryWithTranslation>> {
        return countryDao.getAllCountries().combine(
            translationDao.searchTranslations(
                EntityType.COUNTRY,
                currentLanguage,
                ""
            )
        ) { countries, translations ->
            countries.map { country ->
                val translation = translations.find { it.entityId == country.id }
                CountryWithTranslation(
                    country = country,
                    translatedName = translation?.name ?: country.name,
                    translatedDescription = translation?.description
                )
            }
        }
    }

    suspend fun getCountryDetails(countryId: String): CountryDetails? {
        val country = countryDao.getCountryById(countryId) ?: return null
        val translation = translationDao.getTranslation(
            countryId,
            EntityType.COUNTRY,
            currentLanguage
        )
        return CountryDetails(
            country = country,
            translatedName = translation?.name ?: country.name,
            translatedDescription = translation?.description,
            additionalInfo = translation?.additionalInfo ?: emptyMap()
        )
    }

    // Province Operations
    fun getProvincesForCountry(countryId: String): Flow<List<ProvinceWithTranslation>> {
        return provinceDao.getProvincesByCountry(countryId).combine(
            translationDao.searchTranslations(
                EntityType.PROVINCE,
                currentLanguage,
                ""
            )
        ) { provinces, translations ->
            provinces.map { province ->
                val translation = translations.find { it.entityId == province.id }
                ProvinceWithTranslation(
                    province = province,
                    translatedName = translation?.name ?: province.name,
                    translatedDescription = translation?.description
                )
            }
        }
    }

    // School Operations
    fun getSchoolsForProvince(
        provinceId: String,
        type: SchoolType? = null
    ): Flow<List<SchoolWithTranslation>> {
        val schoolsFlow = type?.let {
            schoolDao.getSchoolsByType(provinceId, it)
        } ?: schoolDao.getSchoolsByProvince(provinceId)

        return schoolsFlow.combine(
            translationDao.searchTranslations(
                EntityType.SCHOOL,
                currentLanguage,
                ""
            )
        ) { schools, translations ->
            schools.map { school ->
                val translation = translations.find { it.entityId == school.id }
                SchoolWithTranslation(
                    school = school,
                    translatedName = translation?.name ?: school.name,
                    translatedDescription = translation?.description ?: school.description,
                    translatedPrograms = translation?.additionalInfo?.get("programs")
                        ?.split(",") ?: school.programs,
                    translatedRequirements = translation?.additionalInfo?.get("requirements")
                        ?.split(",") ?: school.admissionRequirements
                )
            }
        }
    }

    // Search Operations
    fun searchSchools(
        query: String,
        countryId: String? = null,
        provinceId: String? = null,
        type: SchoolType? = null
    ): Flow<List<SchoolWithTranslation>> {
        return when {
            provinceId != null -> {
                if (type != null) {
                    schoolDao.searchSchoolsByType(provinceId, type, query)
                } else {
                    schoolDao.searchSchools(provinceId, query)
                }
            }
            // Add more search combinations as needed
            else -> schoolDao.searchSchools(provinceId ?: "", query)
        }.combine(
            translationDao.searchTranslations(
                EntityType.SCHOOL,
                currentLanguage,
                query
            )
        ) { schools, translations ->
            schools.map { school ->
                val translation = translations.find { it.entityId == school.id }
                SchoolWithTranslation(
                    school = school,
                    translatedName = translation?.name ?: school.name,
                    translatedDescription = translation?.description ?: school.description,
                    translatedPrograms = translation?.additionalInfo?.get("programs")
                        ?.split(",") ?: school.programs,
                    translatedRequirements = translation?.additionalInfo?.get("requirements")
                        ?.split(",") ?: school.admissionRequirements
                )
            }
        }
    }
}

// Data classes for translated responses
data class CountryWithTranslation(
    val country: Country,
    val translatedName: String,
    val translatedDescription: String?
)

data class ProvinceWithTranslation(
    val province: Province,
    val translatedName: String,
    val translatedDescription: String?
)

data class SchoolWithTranslation(
    val school: School,
    val translatedName: String,
    val translatedDescription: String,
    val translatedPrograms: List<String>,
    val translatedRequirements: List<String>
)

data class CountryDetails(
    val country: Country,
    val translatedName: String,
    val translatedDescription: String?,
    val additionalInfo: Map<String, String>
)
// app/src/main/java/com/example/africanschools/network/ApiService.kt
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

// app/src/main/java/com/example/africanschools/network/model/ApiModels.kt
package com.example.africanschools.network.model

data class ApiCountry(
    val id: String,
    val name: String,
    val code: String,
    val flagUrl: String,
    val totalSchools: Int,
    val translations: Map<String, ApiTranslation>
)

data class ApiProvince(
    val id: String,
    val countryId: String,
    val name: String,
    val type: ProvinceType,
    val totalSchools: Int,
    val coordinates: ApiCoordinates,
    val translations: Map<String, ApiTranslation>
)

data class ApiSchool(
    val id: String,
    val provinceId: String,
    val name: String,
    val type: SchoolType,
    val establishedYear: Int,
    val coordinates: ApiCoordinates,
    val translations: Map<String, ApiTranslation>
)

data class ApiSchoolDetails(
    val id: String,
    val provinceId: String,
    val name: String,
    val type: SchoolType,
    val description: String,
    val establishedYear: Int,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String,
    val coordinates: ApiCoordinates,
    val imageUrls: List<String>,
    val facilities: List<String>,
    val programs: List<String>,
    val admissionRequirements: List<String>,
    val applicationDeadlines: Map<String, Long>,
    val academicCalendar: Map<String, String>,
    val translations: Map<String, ApiTranslation>
)

data class ApiTranslation(
    val name: String,
    val description: String?,
    val additionalInfo: Map<String, String>
)

data class ApiCoordinates(
    val latitude: Double,
    val longitude: Double
)

data class ApiPaginatedResponse<T>(
    val items: List<T>,
    val totalItems: Int,
    val page: Int,
    val totalPages: Int
)

// app/src/main/java/com/example/africanschools/network/NetworkModule.kt
package com.example.africanschools.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://api.africanschools.com/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept-Language", "en")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class)
    }
}
// app/src/main/java/com/example/africanschools/repository/MainRepository.kt
package com.example.africanschools.repository

import com.example.africanschools.data.database.DatabaseRepository
import com.example.africanschools.data.model.*
import com.example.africanschools.network.ApiService
import com.example.africanschools.network.model.*
import com.example.africanschools.util.NetworkBoundResource
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiService: ApiService
) {
    // Current language management
    private val currentLanguage = MutableStateFlow(AfricanLanguage.ENGLISH)

    fun setCurrentLanguage(language: AfricanLanguage) {
        currentLanguage.value = language
        databaseRepository.setLanguage(language)
    }

    // Countries
    fun getCountries() = NetworkBoundResource(
        query = {
            databaseRepository.getCountriesWithTranslations()
        },
        fetch = {
            apiService.getCountries(currentLanguage.value.name)
        },
        saveFetchResult = { apiCountries ->
            val countries = apiCountries.map { it.toDomainModel() }
            val translations = apiCountries.flatMap { it.toTranslations() }
            databaseRepository.updateCountriesWithTranslations(countries, translations)
        }
    ).asFlow()

    // Provinces
    fun getProvinces(countryId: String) = NetworkBoundResource(
        query = {
            databaseRepository.getProvincesForCountry(countryId)
        },
        fetch = {
            apiService.getProvinces(countryId, currentLanguage.value.name)
        },
        saveFetchResult = { apiProvinces ->
            val provinces = apiProvinces.map { it.toDomainModel() }
            val translations = apiProvinces.flatMap { it.toTranslations() }
            databaseRepository.updateProvincesWithTranslations(provinces, translations)
        }
    ).asFlow()

    // Schools
    fun getSchools(
        provinceId: String, 
        filters: SchoolFilters
    ) = NetworkBoundResource(
        query = {
            databaseRepository.getSchoolsForProvince(provinceId)
                .map { schools -> filterSchools(schools, filters) }
        },
        fetch = {
            apiService.getSchools(
                provinceId = provinceId,
                schoolType = filters.type,
                language = currentLanguage.value.name
            )
        },
        saveFetchResult = { response ->
            val schools = response.items.map { it.toDomainModel() }
            val translations = response.items.flatMap { it.toTranslations() }
            databaseRepository.updateSchoolsWithTranslations(schools, translations)
        }
    ).asFlow()

    // School Details
    suspend fun getSchoolDetails(schoolId: String): Flow<Resource<SchoolDetails>> = flow {
        emit(Resource.Loading())
        try {
            // First try to get from database
            val localDetails = databaseRepository.getSchoolDetails(schoolId)
            if (localDetails != null) {
                emit(Resource.Success(localDetails))
            }

            // Fetch fresh data from API
            val apiDetails = apiService.getSchoolDetails(
                schoolId = schoolId,
                language = currentLanguage.value.name
            )
            
            // Save to database
            val schoolDetails = apiDetails.toDomainModel()
            val translations = apiDetails.toTranslations()
            databaseRepository.updateSchoolDetailsWithTranslations(schoolDetails, translations)
            
            // Emit fresh data
            emit(Resource.Success(schoolDetails))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Search
    fun searchSchools(
        query: String,
        filters: SearchFilters
    ): Flow<PaginatedResource<List<SchoolWithTranslation>>> = flow {
        emit(PaginatedResource.Loading())
        try {
            // Search in database first
            val localResults = databaseRepository.searchSchools(
                query = query,
                countryId = filters.countryId,
                provinceId = filters.provinceId,
                type = filters.type
            )
            emit(PaginatedResource.Success(localResults, false))

            // Then fetch from network
            val apiResults = apiService.searchSchools(
                query = query,
                countryId = filters.countryId,
                provinceId = filters.provinceId,
                schoolType = filters.type,
                language = currentLanguage.value.name
            )

            // Update database with new results
            val schools = apiResults.items.map { it.toDomainModel() }
            val translations = apiResults.items.flatMap { it.toTranslations() }
            databaseRepository.updateSchoolsWithTranslations(schools, translations)

            // Emit updated results with pagination info
            emit(PaginatedResource.Success(
                data = schools.map { it.toSchoolWithTranslation(translations) },
                hasMorePages = apiResults.page < apiResults.totalPages
            ))
        } catch (e: Exception) {
            emit(PaginatedResource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Favorites
    fun getFavoriteSchools(): Flow<List<SchoolWithTranslation>> {
        return databaseRepository.getFavoriteSchools()
    }

    suspend fun toggleFavorite(schoolId: String) {
        databaseRepository.toggleFavorite(schoolId)
    }

    // Helper Methods
    private fun filterSchools(
        schools: List<SchoolWithTranslation>,
        filters: SchoolFilters
    ): List<SchoolWithTranslation> {
        return schools.filter { school ->
            filters.type?.let { it == school.school.type } ?: true &&
            filters.minEstablishedYear?.let { year -> 
                school.school.establishedYear >= year 
            } ?: true &&
            (filters.facilities.isEmpty() || 
                school.school.facilities.containsAll(filters.facilities)) &&
            (filters.programs.isEmpty() || 
                school.translatedPrograms.any { it in filters.programs })
        }
    }
}

// app/src/main/java/com/example/africanschools/util/Resource.kt
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

// Paginated resource for handling paginated responses
sealed class PaginatedResource<T> {
    class Loading<T> : PaginatedResource<T>()
    data class Success<T>(val data: T, val hasMorePages: Boolean) : PaginatedResource<T>()
    data class Error<T>(val message: String) : PaginatedResource<T>()
}
// app/src/main/java/com/example/africanschools/di/AppModule.kt
package com.example.africanschools.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context) =
        context.getSharedPreferences("african_schools_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideCoroutineDispatcher() = CoroutineDispatcherProvider()
}

// app/src/main/java/com/example/africanschools/di/RepositoryModule.kt
package com.example.africanschools.di

import com.example.africanschools.data.repository.DatabaseRepository
import com.example.africanschools.network.ApiService
import com.example.africanschools.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMainRepository(
        databaseRepository: DatabaseRepository,
        apiService: ApiService
    ): MainRepository = MainRepository(databaseRepository, apiService)
}

// app/src/main/java/com/example/africanschools/di/UseCaseModule.kt
package com.example.africanschools.di

import com.example.africanschools.domain.usecase.*
import com.example.africanschools.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideGetCountriesUseCase(repository: MainRepository) =
        GetCountriesUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetProvincesUseCase(repository: MainRepository) =
        GetProvincesUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetSchoolsUseCase(repository: MainRepository) =
        GetSchoolsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetSchoolDetailsUseCase(repository: MainRepository) =
        GetSchoolDetailsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideSearchSchoolsUseCase(repository: MainRepository) =
        SearchSchoolsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideFavoriteSchoolsUseCase(repository: MainRepository) =
        FavoriteSchoolsUseCase(repository)
}

// app/src/main/java/com/example/africanschools/di/CoroutineDispatcherProvider.kt
package com.example.africanschools.di

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CoroutineDispatcherProvider @Inject constructor() {
    val main = Dispatchers.Main
    val io = Dispatchers.IO
    val default = Dispatchers.Default
}

// app/src/main/java/com/example/africanschools/AfricanSchoolsApplication.kt
package com.example.africanschools

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AfricanSchoolsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
<application
    android:name=".AfricanSchoolsApplication"
    ...>
    @HiltViewModel
class YourViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel()
// app/src/main/java/com/example/africanschools/ui/base/BaseViewModel.kt
package com.example.africanschools.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.africanschools.di.CoroutineDispatcherProvider
import com.example.africanschools.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    @Inject
    lateinit var dispatchers: CoroutineDispatcherProvider

    protected fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _loading.value = true
                block()
            } catch (e: Exception) {
                Timber.e(e)
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false
            }
        }
    }

    protected suspend fun <T> handleResource(
        resource: Resource<T>,
        onSuccess: suspend (T) -> Unit,
        onError: (suspend (String) -> Unit)? = null
    ) {
        when (resource) {
            is Resource.Loading -> _loading.value = true
            is Resource.Success -> {
                _loading.value = false
                onSuccess(resource.data)
            }
            is Resource.Error -> {
                _loading.value = false
                _error.value = resource.message
                onError?.invoke(resource.message)
            }
        }
    }

    protected fun clearError() {
        _error.value = null
    }
}

// app/src/main/java/com/example/africanschools/ui/base/BaseListViewModel.kt
package com.example.africanschools.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseListViewModel<T> : BaseViewModel() {
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems

    protected var currentPage = 1
    protected var isLoadingMore = false

    abstract suspend fun loadItems(forceRefresh: Boolean = false)
    abstract suspend fun loadMoreItems()

    fun refresh() {
        launchCoroutine {
            _refreshing.value = true
            try {
                loadItems(forceRefresh = true)
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (!isLoadingMore && hasMoreItems.value) {
            launchCoroutine {
                isLoadingMore = true
                try {
                    loadMoreItems()
                    currentPage++
                } finally {
                    isLoadingMore = false
                }
            }
        }
    }

    protected fun updateItems(newItems: List<T>, clearExisting: Boolean = false) {
        _items.value = if (clearExisting) {
            newItems
        } else {
            _items.value + newItems
        }
    }

    protected fun setHasMoreItems(hasMore: Boolean) {
        _hasMoreItems.value = hasMore
    }
}

// app/src/main/java/com/example/africanschools/ui/base/BaseSearchViewModel.kt
package com.example.africanschools.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn

abstract class BaseSearchViewModel<T, F> : BaseListViewModel<T>() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filters = MutableStateFlow<F?>(null)
    val filters: StateFlow<F?> = _filters

    protected abstract suspend fun performSearch(
        query: String,
        filters: F?,
        page: Int
    ): List<T>

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        resetSearch()
    }

    fun setFilters(filters: F?) {
        _filters.value = filters
        resetSearch()
    }

    private fun resetSearch() {
        currentPage = 1
        launchCoroutine {
            loadItems(forceRefresh = true)
        }
    }

    override suspend fun loadItems(forceRefresh: Boolean) {
        val results = performSearch(searchQuery.value, filters.value, 1)
        updateItems(results, clearExisting = true)
    }

    override suspend fun loadMoreItems() {
        val results = performSearch(searchQuery.value, filters.value, currentPage + 1)
        updateItems(results)
    }
}

// app/src/main/java/com/example/africanschools/ui/base/ViewState.kt
package com.example.africanschools.ui.base

sealed class ViewState<out T> {
    object Loading : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(val message: String) : ViewState<Nothing>()
}

// app/src/main/java/com/example/africanschools/ui/common/UiEvent.kt
package com.example.africanschools.ui.common

sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    object Refresh : UiEvent()
}
@HiltViewModel
class CountryViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase
) : BaseViewModel() {
    // Implementation
}
@HiltViewModel
class SchoolListViewModel @Inject constructor(
    private val getSchoolsUseCase: GetSchoolsUseCase
) : BaseListViewModel<SchoolWithTranslation>() {
    // Implementation
}
@HiltViewModel
class SchoolSearchViewModel @Inject constructor(
    private val searchSchoolsUseCase: SearchSchoolsUseCase
) : BaseSearchViewModel<SchoolWithTranslation, SearchFilters>() {
    // Implementation
}
// app/src/main/java/com/example/africanschools/navigation/NavDestination.kt
package com.example.africanschools.navigation

sealed class NavDestination(val route: String) {
    object Splash : NavDestination("splash")
    object Countries : NavDestination("countries")
    object CountryDetail : NavDestination("country/{countryId}") {
        fun createRoute(countryId: String) = "country/$countryId"
    }
    object Provinces : NavDestination("country/{countryId}/provinces") {
        fun createRoute(countryId: String) = "country/$countryId/provinces"
    }
    object SchoolList : NavDestination("province/{provinceId}/schools") {
        fun createRoute(provinceId: String) = "province/$provinceId/schools"
    }
    object SchoolDetail : NavDestination("school/{schoolId}") {
        fun createRoute(schoolId: String) = "school/$schoolId"
    }
    object Search : NavDestination("search")
    object Favorites : NavDestination("favorites")
    object Settings : NavDestination("settings")
}

// app/src/main/java/com/example/africanschools/navigation/NavGraph.kt
package com.example.africanschools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.africanschools.ui.screens.*

@Composable
fun AfricanSchoolsNavGraph(
    navController: NavHostController,
    startDestination: String = NavDestination.Splash.route
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavDestination.Splash.route) {
            SplashScreen(
                onSplashComplete = navigationActions.navigateToCountries
            )
        }

        composable(NavDestination.Countries.route) {
            CountriesScreen(
                onCountryClick = navigationActions.navigateToCountryDetail,
                onSearchClick = navigationActions.navigateToSearch,
                onFavoritesClick = navigationActions.navigateToFavorites,
                onSettingsClick = navigationActions.navigateToSettings
            )
        }

        composable(
            route = NavDestination.CountryDetail.route,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val countryId = backStackEntry.arguments?.getString("countryId")!!
            CountryDetailScreen(
                countryId = countryId,
                onNavigateUp = navigationActions.navigateUp,
                onProvinceClick = navigationActions.navigateToProvince
            )
        }

        composable(
            route = NavDestination.Provinces.route,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val countryId = backStackEntry.arguments?.getString("countryId")!!
            ProvincesScreen(
                countryId = countryId,
                onNavigateUp = navigationActions.navigateUp,
                onProvinceClick = { provinceId ->
                    navigationActions.navigateToSchoolList(provinceId)
                }
            )
        }

        composable(
            route = NavDestination.SchoolList.route,
            arguments = listOf(
                navArgument("provinceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val provinceId = backStackEntry.arguments?.getString("provinceId")!!
            SchoolListScreen(
                provinceId = provinceId,
                onNavigateUp = navigationActions.navigateUp,
                onSchoolClick = navigationActions.navigateToSchoolDetail
            )
        }

        composable(
            route = NavDestination.SchoolDetail.route,
            arguments = listOf(
                navArgument("schoolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val schoolId = backStackEntry.arguments?.getString("schoolId")!!
            SchoolDetailScreen(
                schoolId = schoolId,
                onNavigateUp = navigationActions.navigateUp
            )
        }

        composable(NavDestination.Search.route) {
            SearchScreen(
                onNavigateUp = navigationActions.navigateUp,
                onSchoolClick = navigationActions.navigateToSchoolDetail
            )
        }

        composable(NavDestination.Favorites.route) {
            FavoritesScreen(
                onNavigateUp = navigationActions.navigateUp,
                onSchoolClick = navigationActions.navigateToSchoolDetail
            )
        }

        composable(NavDestination.Settings.route) {
            SettingsScreen(
                onNavigateUp = navigationActions.navigateUp
            )
        }
    }
}

// app/src/main/java/com/example/africanschools/navigation/NavigationActions.kt
package com.example.africanschools.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navController: NavHostController) {
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToCountries: () -> Unit = {
        navController.navigate(NavDestination.Countries.route) {
            popUpTo(NavDestination.Splash.route) { inclusive = true }
        }
    }

    val navigateToCountryDetail: (String) -> Unit = { countryId ->
        navController.navigate(NavDestination.CountryDetail.createRoute(countryId))
    }

    val navigateToProvince: (String) -> Unit = { countryId ->
        navController.navigate(NavDestination.Provinces.createRoute(countryId))
    }

    val navigateToSchoolList: (String) -> Unit = { provinceId ->
        navController.navigate(NavDestination.SchoolList.createRoute(provinceId))
    }

    val navigateToSchoolDetail: (String) -> Unit = { schoolId ->
        navController.navigate(NavDestination.SchoolDetail.createRoute(schoolId))
    }

    val navigateToSearch: () -> Unit = {
        navController.navigate(NavDestination.Search.route)
    }

    val navigateToFavorites: () -> Unit = {
        navController.navigate(NavDestination.Favorites.route)
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(NavDestination.Settings.route)
    }
}

// app/src/main/java/com/example/africanschools/ui/MainActivity.kt
package com.example.africanschools.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.africanschools.navigation.AfricanSchoolsNavGraph
import com.example.africanschools.ui.theme.AfricanSchoolsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfricanSchoolsApp()
        }
    }
}

@Composable
fun AfricanSchoolsApp() {
    AfricanSchoolsTheme {
        val navController = rememberNavController()
        AfricanSchoolsNavGraph(navController = navController)
    }
}
// app/src/main/java/com/example/africanschools/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(NetworkConnectivityInterceptor())
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(FlowCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

// app/src/main/java/com/example/africanschools/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AfricanSchoolsDatabase {
        return Room.databaseBuilder(
            context,
            AfricanSchoolsDatabase::class.java,
            "african_schools.db"
        )
            .addCallback(DatabaseCallback())
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides
    fun provideCountryDao(database: AfricanSchoolsDatabase) = database.countryDao()

    @Provides
    fun provideProvinceDao(database: AfricanSchoolsDatabase) = database.provinceDao()

    @Provides
    fun provideSchoolDao(database: AfricanSchoolsDatabase) = database.schoolDao()

    @Provides
    fun provideTranslationDao(database: AfricanSchoolsDatabase) = database.translationDao()
}

// app/src/main/java/com/example/africanschools/di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMainRepository(
        database: AfricanSchoolsDatabase,
        apiService: ApiService,
        @ApplicationContext context: Context
    ): MainRepository = MainRepository(database, apiService, context)

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository = PreferencesRepository(context)
}

// app/src/main/java/com/example/africanschools/di/UseCaseModule.kt
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideGetCountriesUseCase(
        repository: MainRepository,
        dispatchers: CoroutineDispatcherProvider
    ) = GetCountriesUseCase(repository, dispatchers)

    @Provides
    @ViewModelScoped
    fun provideGetProvincesUseCase(
        repository: MainRepository,
        dispatchers: CoroutineDispatcherProvider
    ) = GetProvincesUseCase(repository, dispatchers)

    @Provides
    @ViewModelScoped
    fun provideGetSchoolsUseCase(
        repository: MainRepository,
        dispatchers: CoroutineDispatcherProvider
    ) = GetSchoolsUseCase(repository, dispatchers)

    @Provides
    @ViewModelScoped
    fun provideSearchSchoolsUseCase(
        repository: MainRepository,
        dispatchers: CoroutineDispatcherProvider
    ) = SearchSchoolsUseCase(repository, dispatchers)
}

// app/src/main/java/com/example/africanschools/di/UtilityModule.kt
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {
    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider() = CoroutineDispatcherProvider()

    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ) = ImageLoader(context)

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        @ApplicationContext context: Context
    ) = AnalyticsTracker(context)
}

// app/src/main/java/com/example/africanschools/di/qualifiers/Qualifiers.kt
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

// app/src/main/java/com/example/africanschools/di/NetworkConnectivityInterceptor.kt
class NetworkConnectivityInterceptor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// app/src/main/java/com/example/africanschools/di/AuthInterceptor.kt
class AuthInterceptor @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            preferencesRepository.getAuthToken()?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
// app/src/main/java/com/example/africanschools/ui/base/BaseViewModel.kt
package com.example.africanschools.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.africanschools.di.CoroutineDispatcherProvider
import com.example.africanschools.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {
    @Inject
    lateinit var dispatchers: CoroutineDispatcherProvider

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    protected fun launchWithState(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatchers.io) {
            try {
                if (showLoading) {
                    _uiState.value = UiState.Loading
                }
                block()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                if (showLoading) {
                    _uiState.value = UiState.Idle
                }
            }
        }
    }

    protected suspend fun <T> Flow<Resource<T>>.collectResource(
        onSuccess: suspend (T) -> Unit
    ) {
        collect { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.value = UiState.Loading
                is Resource.Success -> {
                    onSuccess(resource.data)
                    _uiState.value = UiState.Idle
                }
                is Resource.Error -> {
                    handleError(resource.error)
                    _uiState.value = UiState.Idle
                }
            }
        }
    }

    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun handleError(error: Throwable) {
        viewModelScope.launch {
            _uiState.value = UiState.Error(error.message ?: "Unknown error occurred")
            _uiEvent.send(UiEvent.ShowError(error.message ?: "Unknown error occurred"))
        }
    }
}

// app/src/main/java/com/example/africanschools/ui/base/ListViewModel.kt
abstract class ListViewModel<T> : BaseViewModel() {
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    protected var currentPage = 1
    protected var hasMoreItems = true
    protected var isLoadingMore = false

    abstract suspend fun loadItems(forceRefresh: Boolean = false)
    abstract suspend fun loadMoreItems()

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            try {
                loadItems(forceRefresh = true)
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (!isLoadingMore && hasMoreItems) {
            viewModelScope.launch {
                isLoadingMore = true
                try {
                    loadMoreItems()
                    currentPage++
                } finally {
                    isLoadingMore = false
                }
            }
        }
    }

    protected fun updateItems(newItems: List<T>, clearExisting: Boolean = false) {
        _items.value = if (clearExisting) newItems else _items.value + newItems
    }
}

// app/src/main/java/com/example/africanschools/ui/base/SearchViewModel.kt
abstract class SearchViewModel<T, F> : ListViewModel<T>() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow<F?>(null)
    val filters: StateFlow<F?> = _filters.asStateFlow()

    private val _searchDebouncer = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchDebouncer
                .debounce(300)
                .collect {
                    if (it.isNotEmpty()) {
                        performSearch(it)
                    }
                }
        }
    }

    abstract suspend fun performSearch(query: String, filters: F? = null): List<T>

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _searchDebouncer.value = query
    }

    fun setFilters(newFilters: F?) {
        _filters.value = newFilters
        if (_searchQuery.value.isNotEmpty()) {
            viewModelScope.launch {
                performSearch(_searchQuery.value, newFilters)
            }
        }
    }
}

// app/src/main/java/com/example/africanschools/ui/base/UiState.kt
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
}

// app/src/main/java/com/example/africanschools/ui/base/UiEvent.kt
sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class ShowMessage(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

// Example implementation of a specific ViewModel
@HiltViewModel
class SchoolListViewModel @Inject constructor(
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val searchSchoolsUseCase: SearchSchoolsUseCase
) : SearchViewModel<SchoolWithTranslation, SchoolFilters>() {
    
    private var provinceId: String? = null

    fun initialize(newProvinceId: String) {
        if (provinceId != newProvinceId) {
            provinceId = newProvinceId
            refresh()
        }
    }

    override suspend fun loadItems(forceRefresh: Boolean) {
        provinceId?.let { id ->
            getSchoolsUseCase(id, forceRefresh)
                .collectResource { schools ->
                    updateItems(schools, clearExisting = true)
                }
        }
    }

    override suspend fun loadMoreItems() {
        provinceId?.let { id ->
            getSchoolsUseCase(id, page = currentPage + 1)
                .collectResource { schools ->
                    updateItems(schools)
                    hasMoreItems = schools.isNotEmpty()
                }
        }
    }

    override suspend fun performSearch(query: String, filters: SchoolFilters?) {
        provinceId?.let { id ->
            searchSchoolsUseCase(
                query = query,
                provinceId = id,
                filters = filters
            ).collectResource { schools ->
                updateItems(schools, clearExisting = true)
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/navigation/NavGraph.kt
package com.example.africanschools.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object CountryList : Screen("countries")
    object ProvinceList : Screen("country/{countryId}/provinces") {
        fun createRoute(countryId: String) = "country/$countryId/provinces"
    }
    object SchoolList : Screen("province/{provinceId}/schools") {
        fun createRoute(provinceId: String) = "province/$provinceId/schools"
    }
    object SchoolDetail : Screen("school/{schoolId}") {
        fun createRoute(schoolId: String) = "school/$schoolId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
}

@Composable
fun AfricanSchoolsNavigation(
    startDestination: String = Screen.Splash.route,
    navController: NavHostController = rememberNavController()
) {
    val actions = remember(navController) { NavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = actions.navigateToCountryList
            )
        }

        composable(Screen.CountryList.route) {
            CountryListScreen(
                onCountryClick = actions.navigateToProvinceList,
                onSearchClick = actions.navigateToSearch,
                onSettingsClick = actions.navigateToSettings
            )
        }

        composable(
            route = Screen.ProvinceList.route,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val countryId = backStackEntry.arguments?.getString("countryId")!!
            ProvinceListScreen(
                countryId = countryId,
                onProvinceClick = actions.navigateToSchoolList,
                onNavigateUp = actions.navigateUp
            )
        }

        composable(
            route = Screen.SchoolList.route,
            arguments = listOf(
                navArgument("provinceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val provinceId = backStackEntry.arguments?.getString("provinceId")!!
            SchoolListScreen(
                provinceId = provinceId,
                onSchoolClick = actions.navigateToSchoolDetail,
                onNavigateUp = actions.navigateUp
            )
        }

        composable(
            route = Screen.SchoolDetail.route,
            arguments = listOf(
                navArgument("schoolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val schoolId = backStackEntry.arguments?.getString("schoolId")!!
            SchoolDetailScreen(
                schoolId = schoolId,
                onNavigateUp = actions.navigateUp
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSchoolClick = actions.navigateToSchoolDetail,
                onNavigateUp = actions.navigateUp
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateUp = actions.navigateUp
            )
        }
    }
}

// app/src/main/java/com/example/africanschools/navigation/NavigationActions.kt
class NavigationActions(private val navController: NavHostController) {
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToCountryList: () -> Unit = {
        navController.navigate(Screen.CountryList.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val navigateToProvinceList: (String) -> Unit = { countryId ->
        navController.navigate(Screen.ProvinceList.createRoute(countryId))
    }

    val navigateToSchoolList: (String) -> Unit = { provinceId ->
        navController.navigate(Screen.SchoolList.createRoute(provinceId))
    }

    val navigateToSchoolDetail: (String) -> Unit = { schoolId ->
        navController.navigate(Screen.SchoolDetail.createRoute(schoolId))
    }

    val navigateToSearch: () -> Unit = {
        navController.navigate(Screen.Search.route)
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(Screen.Settings.route)
    }
}

// app/src/main/java/com/example/africanschools/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfricanSchoolsTheme {
                AfricanSchoolsNavigation()
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/country/CountryListScreen.kt
package com.example.africanschools.ui.screens.country

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun CountryListScreen(
    onCountryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: CountryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CountryListTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                onSortClick = { showSortDialog = true },
                onViewTypeClick = viewModel::toggleViewType
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState is UiState.Loading && countries.isEmpty() -> {
                    LoadingPlaceholder(viewType)
                }
                uiState is UiState.Error && countries.isEmpty() -> {
                    ErrorState(
                        message = (uiState as UiState.Error).message,
                        onRetry = viewModel::loadCountries
                    )
                }
                countries.isEmpty() -> {
                    EmptyState(
                        searchQuery = searchQuery,
                        onClearSearch = { viewModel.setSearchQuery("") }
                    )
                }
                else -> {
                    CountryContent(
                        countries = countries,
                        viewType = viewType,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        onCountryClick = onCountryClick
                    )
                }
            }

            // FAB for quick scrolling to top
            if (countries.isNotEmpty()) {
                FloatingActionButton(
                    onClick = viewModel::scrollToTop,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, "Scroll to top")
                }
            }
        }

        if (showSortDialog) {
            SortDialog(
                currentSort = sortOrder,
                onSortSelected = { 
                    viewModel.setSortOrder(it)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
    }
}

@Composable
private fun CountryListTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSortClick: () -> Unit,
    onViewTypeClick: () -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }

    if (showSearch) {
        SearchTopBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onCloseClick = { 
                showSearch = false
                onSearchQueryChange("")
            }
        )
    } else {
        TopAppBar(
            title = { Text("African Schools") },
            actions = {
                IconButton(onClick = { showSearch = true }) {
                    Icon(Icons.Default.Search, "Search")
                }
                IconButton(onClick = onSortClick) {
                    Icon(Icons.Default.Sort, "Sort")
                }
                IconButton(onClick = onViewTypeClick) {
                    Icon(Icons.Default.ViewList, "Change view")
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
        )
    }
}

@Composable
private fun CountryContent(
    countries: List<CountryUiModel>,
    viewType: ViewType,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCountryClick: (String) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh
    ) {
        when (viewType) {
            ViewType.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(
                        items = countries,
                        key = { it.id }
                    ) { country ->
                        CountryGridItem(
                            country = country,
                            onClick = { onCountryClick(country.id) }
                        )
                    }
                }
            }
            ViewType.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = countries,
                        key = { it.id }
                    ) { country ->
                        CountryListItem(
                            country = country,
                            onClick = { onCountryClick(country.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryGridItem(
    country: CountryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column {
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "Flag of ${country.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${country.totalSchools} schools",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                LinearProgressIndicator(
                    progress = country.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
private fun SortDialog(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Countries") },
        text = {
            Column {
                SortOrder.values().forEach { sortOrder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(sortOrder) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == sortOrder,
                            onClick = { onSortSelected(sortOrder) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sortOrder.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// app/src/main/java/com/example/africanschools/ui/screens/country/CountryListViewModel.kt
@HiltViewModel
class CountryListViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val preferencesRepository: PreferencesRepository
) : BaseViewModel() {
    private val _countries = MutableStateFlow<List<CountryUiModel>>(emptyList())
    val countries: StateFlow<List<CountryUiModel>> = _countries.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _viewType = MutableStateFlow(ViewType.GRID)
    val viewType: StateFlow<ViewType> = _viewType.asStateFlow()

    private var originalCountries = listOf<CountryUiModel>()

    init {
        loadCountries()
        loadPreferences()
        observeSearchQuery()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _sortOrder.value = preferencesRepository.getCountrySortOrder()
            _viewType.value = preferencesRepository.getCountryViewType()
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    filterAndSortCountries(query, _sortOrder.value)
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            _sortOrder.value = order
            preferencesRepository.setCountrySortOrder(order)
            filterAndSortCountries(_searchQuery.value, order)
        }
    }

    fun toggleViewType() {
        viewModelScope.launch {
            val newViewType = if (_viewType.value == ViewType.GRID) {
                ViewType.LIST
            } else {
                ViewType.GRID
            }
            _viewType.value = newViewType
            preferencesRepository.setCountryViewType(newViewType)
        }
    }

    private fun filterAndSortCountries(query: String, sortOrder: SortOrder) {
        val filtered = if (query.isEmpty()) {
            originalCountries
        } else {
            originalCountries.filter { 
                it.name.contains(query, ignoreCase = true)
            }
        }

        _countries.value = when (sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name }
            SortOrder.SCHOOLS_ASC -> filtered.sortedBy { it.totalSchools }
            SortOrder.SCHOOLS_DESC -> filtered.sortedByDescending { it.totalSchools }
        }
    }

    fun loadCountries() {
        launchWithState {
            getCountriesUseCase()
                .collectResource { countries ->
                    originalCountries = countries.map { it.toUiModel() }
                    filterAndSortCountries(_searchQuery.value, _sortOrder.value)
                }
        }
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/country/CountryModels.kt
enum class ViewType {
    LIST,
    GRID
}

enum class SortOrder(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SCHOOLS_ASC("Schools (Low to High)"),
    SCHOOLS_DESC("Schools (High to Low)")
}

data class CountryUiModel(
    val id: String,
    val name: String,
    val flagUrl: String,
    val totalSchools: Int,
    val progress: Float = 0f
)
// app/src/main/java/com/example/africanschools/ui/screens/province/ProvinceListScreen.kt
@Composable
fun ProvinceListScreen(
    countryId: String,
    onProvinceClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ProvinceListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val provinces by viewModel.provinces.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }

    LaunchedEffect(countryId) {
        viewModel.initialize(countryId)
    }

    Scaffold(
        topBar = {
            ProvinceListTopBar(
                countryName = viewModel.countryName,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onNavigateUp = onNavigateUp,
                onViewModeChange = viewModel::toggleViewMode,
                onFilterClick = { showFilterSheet = true },
                onStatisticsClick = { showStatistics = true }
            )
        },
        floatingActionButton = {
            if (provinces.isNotEmpty()) {
                ProvinceActionButtons(
                    viewMode = viewMode,
                    onMapStyleChange = viewModel::cycleMapStyle,
                    onListStyleChange = viewModel::cycleListStyle
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState is UiState.Loading && provinces.isEmpty() -> {
                    LoadingIndicator()
                }
                uiState is UiState.Error && provinces.isEmpty() -> {
                    ErrorState(
                        message = (uiState as UiState.Error).message,
                        onRetry = { viewModel.loadProvinces(countryId) }
                    )
                }
                provinces.isEmpty() && searchQuery.isEmpty() && filters.isEmpty() -> {
                    EmptyState()
                }
                provinces.isEmpty() -> {
                    NoResultsState(
                        hasFilters = filters.isNotEmpty(),
                        onClearFilters = viewModel::clearFilters
                    )
                }
                else -> {
                    ProvinceContent(
                        provinces = provinces,
                        viewMode = viewMode,
                        mapStyle = viewModel.currentMapStyle,
                        listStyle = viewModel.currentListStyle,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        onProvinceClick = onProvinceClick,
                        onClusterClick = viewModel::handleClusterClick
                    )
                }
            }

            // Active filters indicator
            if (filters.isNotEmpty()) {
                ActiveFiltersIndicator(
                    filterCount = filters.size,
                    onClick = { showFilterSheet = true }
                )
            }
        }
    }

    // Bottom sheets and dialogs
    if (showFilterSheet) {
        ProvinceFilterSheet(
            currentFilters = filters,
            statistics = statistics,
            onFilterChange = viewModel::updateFilters,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showStatistics) {
        ProvinceStatisticsDialog(
            statistics = statistics,
            onDismiss = { showStatistics = false }
        )
    }
}

@Composable
private fun ProvinceFilterSheet(
    currentFilters: ProvinceFilters,
    statistics: ProvinceStatistics,
    onFilterChange: (ProvinceFilters) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Filter Provinces",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Province Type Filter
                FilterSection(
                    title = "Province Type",
                    content = {
                        ProvinceTypeFilter(
                            selectedTypes = currentFilters.types,
                            onTypeSelected = { type, selected ->
                                onFilterChange(currentFilters.copy(
                                    types = if (selected) {
                                        currentFilters.types + type
                                    } else {
                                        currentFilters.types - type
                                    }
                                ))
                            }
                        )
                    }
                )

                // School Count Range Filter
                FilterSection(
                    title = "Number of Schools",
                    content = {
                        SchoolCountRangeFilter(
                            range = currentFilters.schoolCountRange,
                            maxSchools = statistics.maxSchoolsInProvince,
                            onRangeChange = { range ->
                                onFilterChange(currentFilters.copy(
                                    schoolCountRange = range
                                ))
                            }
                        )
                    }
                )

                // Region Filter
                FilterSection(
                    title = "Region",
                    content = {
                        RegionFilter(
                            selectedRegions = currentFilters.regions,
                            availableRegions = statistics.regions,
                            onRegionSelected = { region, selected ->
                                onFilterChange(currentFilters.copy(
                                    regions = if (selected) {
                                        currentFilters.regions + region
                                    } else {
                                        currentFilters.regions - region
                                    }
                                ))
                            }
                        )
                    }
                )

                // Sort Order
                FilterSection(
                    title = "Sort By",
                    content = {
                        SortOrderSelector(
                            currentSort = currentFilters.sortOrder,
                            onSortSelected = { sort ->
                                onFilterChange(currentFilters.copy(
                                    sortOrder = sort
                                ))
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onFilterChange(ProvinceFilters())
                        }
                    ) {
                        Text("Clear All")
                    }
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        },
        sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded),
        onDismiss = onDismiss
    ) {}
}

@Composable
private fun ProvinceStatisticsDialog(
    statistics: ProvinceStatistics,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Province Statistics",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(16.dp))

                StatisticsSection(
                    title = "General Statistics",
                    items = listOf(
                        "Total Provinces" to statistics.totalProvinces.toString(),
                        "Total Schools" to statistics.totalSchools.toString(),
                        "Average Schools per Province" to statistics.averageSchoolsPerProvince.toString()
                    )
                )

                StatisticsSection(
                    title = "Province Types",
                    items = statistics.provinceTypeCounts.map {
                        it.key.name to it.value.toString()
                    }
                )

                StatisticsSection(
                    title = "School Distribution",
                    items = listOf(
                        "Highest" to "${statistics.maxSchoolsInProvince} schools",
                        "Lowest" to "${statistics.minSchoolsInProvince} schools",
                        "Median" to "${statistics.medianSchoolsPerProvince} schools"
                    )
                )

                // Regional Distribution Chart
                RegionalDistributionChart(
                    data = statistics.regionalDistribution,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/province/ProvinceListViewModel.kt
@HiltViewModel
class ProvinceListViewModel @Inject constructor(
    private val getProvincesUseCase: GetProvincesUseCase,
    private val getCountryDetailsUseCase: GetCountryDetailsUseCase,
    private val getProvinceStatisticsUseCase: GetProvinceStatisticsUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseViewModel() {
    private val _provinces = MutableStateFlow<List<ProvinceUiModel>>(emptyList())
    val provinces: StateFlow<List<ProvinceUiModel>> = _provinces.asStateFlow()

    private val _statistics = MutableStateFlow(ProvinceStatistics())
    val statistics: StateFlow<ProvinceStatistics> = _statistics.asStateFlow()

    private val _filters = MutableStateFlow(ProvinceFilters())
    val filters: StateFlow<ProvinceFilters> = _filters.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _mapStyle = MutableStateFlow(MapStyle.STANDARD)
    val mapStyle: StateFlow<MapStyle> = _mapStyle.asStateFlow()

    private val _listStyle = MutableStateFlow(ListStyle.CARD)
    val listStyle: StateFlow<ListStyle> = _listStyle.asStateFlow()

    private var originalProvinces = listOf<ProvinceUiModel>()
    private var currentCountryId: String? = null
    var countryName: String = ""
        private set

    init {
        observeSearchQuery()
        loadPreferences()
    }

    fun initialize(countryId: String) {
        if (currentCountryId != countryId) {
            currentCountryId = countryId
            loadCountryDetails(countryId)
            loadProvinces(countryId)
            loadStatistics(countryId)
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _viewMode.value = preferencesRepository.getProvinceViewMode()
            _mapStyle.value = preferencesRepository.getMapStyle()
            _listStyle.value = preferencesRepository.getListStyle()
        }
    }

    private fun loadCountryDetails(countryId: String) {
        launchWithState {
            getCountryDetailsUseCase(countryId)
                .collectResource { details ->
                    countryName = details.name
                }
        }
    }

    private fun loadProvinces(countryId: String) {
        launchWithState {
            getProvincesUseCase(countryId)
                .collectResource { provinces ->
                    originalProvinces = provinces.map { it.toUiModel() }
                    applyFiltersAndSort()
                }
        }
    }

    private fun loadStatistics(countryId: String) {
        launchWithState(showLoading = false) {
            getProvinceStatisticsUseCase(countryId)
                .collectResource { stats ->
                    _statistics.value = stats
                }
        }
    }

    fun updateFilters(newFilters: ProvinceFilters) {
        _filters.value = newFilters
        applyFiltersAndSort()
        trackFilterUsage(newFilters)
    }

    fun clearFilters() {
        _filters.value = ProvinceFilters()
        applyFiltersAndSort()
        analyticsTracker.trackEvent("filters_cleared")
    }

    private fun applyFiltersAndSort() {
        val currentFilters = _filters.value
        val searchQuery = _searchQuery.value

        val filtered = originalProvinces
            .filter { province ->
                (searchQuery.isEmpty() || province.name.contains(searchQuery, ignoreCase = true)) &&
                (currentFilters.types.isEmpty() || province.type in currentFilters.types) &&
                (currentFilters.regions.isEmpty() || province.region in currentFilters.regions) &&
                (province.totalSchools in currentFilters.schoolCountRange)
            }

        _provinces.value = when (currentFilters.sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name }
            SortOrder.SCHOOLS_ASC -> filtered.sortedBy { it.totalSchools }
            SortOrder.SCHOOLS_DESC -> filtered.sortedByDescending { it.totalSchools }
            SortOrder.AREA_ASC -> filtered.sortedBy { it.area }
            SortOrder.AREA_DESC -> filtered.sortedByDescending { it.area }
        }
    }

    fun toggleViewMode() {
        val newMode = when (_viewMode.value) {
            ViewMode.LIST -> ViewMode.MAP
            ViewMode.MAP -> ViewMode.LIST
        }
        _viewMode.value = newMode
        viewModelScope.launch {
            preferencesRepository.setProvinceViewMode(newMode)
        }
        analyticsTracker.trackEvent("view_mode_changed", mapOf("mode" to newMode.name))
    }

    fun cycleMapStyle() {
        val newStyle = _mapStyle.value.next()
        _mapStyle.value = newStyle
        viewModelScope.launch {
            preferencesRepository.setMapStyle(newStyle)
        }
    }

    fun cycleListStyle() {
        val newStyle = _listStyle.value.next()
        _listStyle.value = newStyle
        viewModelScope.launch {
            preferencesRepository.setListStyle(newStyle)
        }
    }

    private fun trackFilterUsage(filters: ProvinceFilters) {
        analyticsTracker.trackEvent(
            "filters_applied",
            mapOf(
                "types" to filters.types.size,
                "regions" to filters.regions.size,
                "has_school_range" to (filters.schoolCountRange != 0..Int.MAX_VALUE),
                "sort_order" to filters.sortOrder.name
            )
        )
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/province/models/ProvinceModels.kt
data class ProvinceUiModel(
    val id: String,
    val name: String,
    val type: ProvinceType,
    val region: String,
    val totalSchools: Int,
    val area: Double,
    val population: Int,
    val density: Double,
    val latitude: Double,
    val longitude: Double,
    val schoolTypes: Map<SchoolType, Int>,
    val progress: Float,
    val statistics: ProvinceDetailedStats
)

data class ProvinceDetailedStats(
    val primarySchools: Int,
    val secondarySchools: Int,
    val universities: Int,
    val technicalSchools: Int,
    val studentPopulation: Int,
    val teacherCount: Int,
    val averageClassSize: Int,
    val schoolsPerCapita: Double
)

data class ProvinceStatistics(
    val totalProvinces: Int = 0,
    val totalSchools: Int = 0,
    val averageSchoolsPerProvince: Double = 0.0,
    val medianSchoolsPerProvince: Int = 0,
    val maxSchoolsInProvince: Int = 0,
    val minSchoolsInProvince: Int = 0,
    val provinceTypeCounts: Map<ProvinceType, Int> = emptyMap(),
    val regions: Set<String> = emptySet(),
    val regionalDistribution: Map<String, Int> = emptyMap(),
    val schoolTypeDistribution: Map<SchoolType, Int> = emptyMap(),
    val populationStats: PopulationStats = PopulationStats(),
    val densityStats: DensityStats = DensityStats(),
    val trends: TrendStats = TrendStats()
)

data class PopulationStats(
    val totalPopulation: Int = 0,
    val averagePopulation: Double = 0.0,
    val medianPopulation: Int = 0,
    val populationRange: IntRange = 0..0
)

data class DensityStats(
    val averageDensity: Double = 0.0,
    val medianDensity: Double = 0.0,
    val densityRange: ClosedRange<Double> = 0.0..0.0
)

data class TrendStats(
    val schoolGrowthRate: Double = 0.0,
    val populationGrowthRate: Double = 0.0,
    val yearOverYearChange: Map<Int, Double> = emptyMap()
)

data class ProvinceFilters(
    val types: Set<ProvinceType> = emptySet(),
    val regions: Set<String> = emptySet(),
    val schoolCountRange: IntRange = 0..Int.MAX_VALUE,
    val populationRange: IntRange = 0..Int.MAX_VALUE,
    val densityRange: ClosedRange<Double> = 0.0..Double.MAX_VALUE,
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

enum class MapStyle {
    STANDARD, SATELLITE, TERRAIN, NIGHT;

    fun next() = values()[(ordinal + 1) % values().size]
}

enum class ListStyle {
    CARD, COMPACT, DETAILED;

    fun next() = values()[(ordinal + 1) % values().size]
}

enum class SortOrder {
    NAME_ASC, NAME_DESC,
    SCHOOLS_ASC, SCHOOLS_DESC,
    AREA_ASC, AREA_DESC
}
// app/src/main/java/com/example/africanschools/ui/screens/province/components/charts/ProvinceCharts.kt
@Composable
fun RegionalDistributionChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val entries = data.entries.toList()
    val maxValue = entries.maxOfOrNull { it.value } ?: 0
    val barWidth = with(LocalDensity.current) { 24.dp.toPx() }
    val animatedValues = entries.map { entry ->
        val animatedValue by animateFloatAsState(
            targetValue = entry.value.toFloat(),
            animationSpec = tween(1000)
        )
        entry.key to animatedValue
    }

    Canvas(modifier = modifier) {
        val availableWidth = size.width - barWidth
        val barSpacing = availableWidth / (entries.size + 1)
        val heightRatio = size.height / maxValue

        entries.forEachIndexed { index, entry ->
            val x = barSpacing * (index + 1)
            val height = animatedValues[index].second * heightRatio

            // Draw bar
            drawRect(
                color = MaterialTheme.colors.primary,
                topLeft = Offset(x, size.height - height),
                size = Size(barWidth, height)
            )

            // Draw label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    entry.key,
                    x,
                    size.height + 20f,
                    Paint().apply {
                        textAlign = Paint.Align.CENTER
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.BLACK
                    }
                )
            }
        }
    }
}

@Composable
fun SchoolTypeDistributionPieChart(
    data: Map<SchoolType, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()
    val angles = data.mapValues { (360f * it.value) / total }
    val colors = SchoolType.values().associateWith { MaterialTheme.colors.random() }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = 0f

        angles.forEach { (type, angle) ->
            drawArc(
                color = colors[type] ?: Color.Gray,
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true,
                topLeft = center - Offset(radius, radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += angle
        }
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/province/components/filters/AdvancedFilters.kt
@Composable
fun AdvancedFilterSection(
    filters: ProvinceFilters,
    statistics: ProvinceStatistics,
    onFiltersChanged: (ProvinceFilters) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // School Count Range Filter
        RangeSliderFilter(
            title = "Number of Schools",
            currentRange = filters.schoolCountRange,
            valueRange = 0..statistics.maxSchoolsInProvince,
            onRangeChange = { range ->
                onFiltersChanged(filters.copy(schoolCountRange = range))
            }
        )

        // Population Range Filter
        RangeSliderFilter(
            title = "Population",
            currentRange = filters.populationRange,
            valueRange = statistics.populationStats.populationRange,
            onRangeChange = { range ->
                onFiltersChanged(filters.copy(populationRange = range))
            }
        )

        // Density Range Filter
        RangeSliderFilter(
            title = "Population Density",
            currentRange = filters.densityRange,
            valueRange = statistics.densityStats.densityRange,
            onRangeChange = { range ->
                onFiltersChanged(filters.copy(densityRange = range))
            }
        )

        // School Type Distribution Filter
        SchoolTypeFilter(
            distribution = statistics.schoolTypeDistribution,
            onTypeSelected = { type, selected ->
                val newTypes = if (selected) {
                    filters.types + type
                } else {
                    filters.types - type
                }
                onFiltersChanged(filters.copy(types = newTypes))
            }
        )
    }
}

@Composable
fun RangeSliderFilter(
    title: String,
    currentRange: ClosedRange<Float>,
    valueRange: ClosedRange<Float>,
    onRangeChange: (ClosedRange<Float>) -> Unit
) {
    var sliderPosition by remember {
        mutableStateOf(currentRange.start..currentRange.endInclusive)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1
        )
        
        RangeSlider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = valueRange.start..valueRange.endInclusive,
            onValueChangeFinished = { onRangeChange(sliderPosition) }
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = sliderPosition.start.toInt().toString(),
                style = MaterialTheme.typography.caption
            )
            Text(
                text = sliderPosition.endInclusive.toInt().toString(),
                style = MaterialTheme.typography.caption
            )
        }
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/province/components/map/ClusteringManager.kt
class ProvinceClusteringManager(
    private val context: Context
) {
    private val algorithm = NonHierarchicalDistanceBasedAlgorithm<ProvinceClusterItem>()
    private val clusterManager = ClusterManager<ProvinceClusterItem>(context, null)

    fun setItems(provinces: List<ProvinceUiModel>) {
        algorithm.clearItems()
        algorithm.addItems(provinces.map { ProvinceClusterItem(it) })
    }

    fun getClusters(zoom: Float): List<Cluster<ProvinceClusterItem>> {
        return algorithm.getClusters(zoom)
    }

    inner class ProvinceClusterItem(
        private val province: ProvinceUiModel
    ) : ClusterItem {
        override fun getPosition(): LatLng =
            LatLng(province.latitude, province.longitude)

        override fun getTitle(): String = province.name

        override fun getSnippet(): String =
            "${province.totalSchools} schools"
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/province/components/ProvinceMapView.kt
@Composable
fun ProvinceMapView(
    provinces: List<ProvinceUiModel>,
    mapStyle: MapStyle,
    onProvinceClick: (String) -> Unit,
    onClusterClick: (List<ProvinceUiModel>) -> Unit
) {
    val context = LocalContext.current
    val clusterManager = remember { ProvinceClusteringManager(context) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(provinces) {
        clusterManager.setItems(provinces)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = when (mapStyle) {
                MapStyle.STANDARD -> MapType.NORMAL
                MapStyle.SATELLITE -> MapType.SATELLITE
                MapStyle.TERRAIN -> MapType.TERRAIN
                MapStyle.NIGHT -> MapType.NORMAL
            },
            mapStyleOptions = if (mapStyle == MapStyle.NIGHT) {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
            } else null
        )
    ) {
        val clusters = clusterManager.getClusters(cameraPositionState.position.zoom)
        
        clusters.forEach { cluster ->
            if (cluster.size > 1) {
                // Render cluster marker
                ClusterMarker(
                    position = cluster.position,
                    clusterSize = cluster.size,
                    onClick = {
                        onClusterClick(cluster.items.map { it.province })
                        true
                    }
                )
            } else {
                // Render individual province marker
                ProvinceMarker(
                    province = cluster.items.first().province,
                    onClick = { onProvinceClick(it.id) }
                )
            }
        }
    }
}

@Composable
fun ClusterMarker(
    position: LatLng,
    clusterSize: Int,
    onClick: () -> Boolean
) {
    MarkerInfoWindow(
        state = MarkerState(position = position),
        onClick = { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = clusterSize.toString(),
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/SchoolListScreen.kt
@Composable
fun SchoolListScreen(
    provinceId: String,
    onSchoolClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SchoolListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val provinceDetails by viewModel.provinceDetails.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(provinceId) {
        viewModel.initialize(provinceId)
    }

    Scaffold(
        topBar = {
            SchoolListTopBar(
                provinceName = provinceDetails?.name ?: "",
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onNavigateUp = onNavigateUp,
                onFilterClick = { showFilterSheet = true },
                onSortClick = { showSortDialog = true },
                onViewModeChange = viewModel::toggleViewMode
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = schools.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                FloatingActionButton(
                    onClick = viewModel::scrollToTop
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, "Scroll to top")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState is UiState.Loading && schools.isEmpty() -> {
                    LoadingIndicator()
                }
                uiState is UiState.Error && schools.isEmpty() -> {
                    ErrorState(
                        message = (uiState as UiState.Error).message,
                        onRetry = { viewModel.loadSchools(provinceId) }
                    )
                }
                schools.isEmpty() && searchQuery.isEmpty() && !filters.hasActiveFilters -> {
                    EmptyState()
                }
                schools.isEmpty() -> {
                    NoResultsState(
                        hasFilters = filters.hasActiveFilters,
                        onClearFilters = viewModel::clearFilters
                    )
                }
                else -> {
                    SchoolContent(
                        schools = schools,
                        viewMode = viewMode,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        onSchoolClick = onSchoolClick,
                        listState = viewModel.listState
                    )
                }
            }

            // Active filters indicator
            if (filters.hasActiveFilters) {
                ActiveFiltersIndicator(
                    filterCount = filters.activeFilterCount,
                    onClick = { showFilterSheet = true }
                )
            }
        }
    }

    // Bottom sheets and dialogs
    if (showFilterSheet) {
        SchoolFilterSheet(
            currentFilters = filters,
            schoolTypes = viewModel.availableSchoolTypes,
            onFilterChange = viewModel::updateFilters,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showSortDialog) {
        SortDialog(
            currentSort = filters.sortOrder,
            onSortSelected = { 
                viewModel.updateSortOrder(it)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
private fun SchoolContent(
    schools: List<SchoolUiModel>,
    viewMode: ViewMode,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSchoolClick: (String) -> Unit,
    listState: LazyListState
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh
    ) {
        when (viewMode) {
            ViewMode.GRID -> {
                SchoolGrid(
                    schools = schools,
                    onSchoolClick = onSchoolClick,
                    listState = listState
                )
            }
            ViewMode.LIST -> {
                SchoolList(
                    schools = schools,
                    onSchoolClick = onSchoolClick,
                    listState = listState
                )
            }
        }
    }
}

@Composable
private fun SchoolGrid(
    schools: List<SchoolUiModel>,
    onSchoolClick: (String) -> Unit,
    listState: LazyListState
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        state = listState.toLazyGridState(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = schools,
            key = { it.id }
        ) { school ->
            SchoolGridItem(
                school = school,
                onClick = { onSchoolClick(school.id) }
            )
        }
    }
}

@Composable
private fun SchoolGridItem(
    school: SchoolUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column {
            AsyncImage(
                model = school.mainImageUrl,
                contentDescription = "Image of ${school.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = school.name,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SchoolTypeChip(type = school.type)
                    if (school.rating > 0) {
                        RatingIndicator(rating = school.rating)
                    }
                }
                if (school.applicationDeadline != null) {
                    DeadlineIndicator(deadline = school.applicationDeadline)
                }
            }
        }
    }
}

@Composable
private fun SchoolList(
    schools: List<SchoolUiModel>,
    onSchoolClick: (String) -> Unit,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = schools,
            key = { it.id }
        ) { school ->
            SchoolListItem(
                school = school,
                onClick = { onSchoolClick(school.id) }
            )
        }
    }
}

@Composable
private fun SchoolListItem(
    school: SchoolUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = school.mainImageUrl,
                contentDescription = "Image of ${school.name}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = school.name,
                    style = MaterialTheme.typography.subtitle1
                )
                SchoolTypeChip(type = school.type)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (school.rating > 0) {
                        RatingIndicator(rating = school.rating)
                    }
                    if (school.applicationDeadline != null) {
                        DeadlineIndicator(deadline = school.applicationDeadline)
                    }
                }
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/SchoolListViewModel.kt
@HiltViewModel
class SchoolListViewModel @Inject constructor(
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val getProvinceDetailsUseCase: GetProvinceDetailsUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseViewModel() {

    private val _schools = MutableStateFlow<List<SchoolUiModel>>(emptyList())
    val schools: StateFlow<List<SchoolUiModel>> = _schools.asStateFlow()

    private val _filters = MutableStateFlow(SchoolFilters())
    val filters: StateFlow<SchoolFilters> = _filters.asStateFlow()

    private val _provinceDetails = MutableStateFlow<ProvinceDetails?>(null)
    val provinceDetails: StateFlow<ProvinceDetails?> = _provinceDetails.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val listState = LazyListState()
    private var originalSchools = listOf<SchoolUiModel>()
    private var currentProvinceId: String? = null

    val availableSchoolTypes = mutableStateOf<Set<SchoolType>>(emptySet())

    init {
        observeSearchQuery()
        loadPreferences()
    }

    fun initialize(provinceId: String) {
        if (currentProvinceId != provinceId) {
            currentProvinceId = provinceId
            loadProvinceDetails(provinceId)
            loadSchools(provinceId)
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _viewMode.value = preferencesRepository.getSchoolViewMode()
            _filters.value = preferencesRepository.getSchoolFilters()
        }
    }

    private fun loadProvinceDetails(provinceId: String) {
        launchWithState {
            getProvinceDetailsUseCase(provinceId)
                .collectResource { details ->
                    _provinceDetails.value = details
                }
        }
    }

    fun loadSchools(provinceId: String) {
        launchWithState {
            getSchoolsUseCase(provinceId)
                .collectResource { schools ->
                    originalSchools = schools.map { it.toUiModel() }
                    updateAvailableSchoolTypes()
                    applyFiltersAndSort()
                }
        }
    }

    private fun updateAvailableSchoolTypes() {
        availableSchoolTypes.value = originalSchools
            .map { it.type }
            .toSet()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    applyFiltersAndSort()
                    trackSearch(query)
                }
        }
    }

    fun updateFilters(newFilters: SchoolFilters) {
        _filters.value = newFilters
        applyFiltersAndSort()
        saveFilters(newFilters)
        trackFilters(newFilters)
    }

    fun clearFilters() {
        _filters.value = SchoolFilters()
        applyFiltersAndSort()
        analyticsTracker.trackEvent("schools_filters_cleared")
    }

    private fun applyFiltersAndSort() {
        val currentFilters = _filters.value
        val query = _searchQuery.value.lowercase()

        val filtered = originalSchools.filter { school ->
            val matchesSearch = query.isEmpty() ||
                school.name.lowercase().contains(query) ||
                school.description.lowercase().contains(query)

            val matchesType = currentFilters.types.isEmpty() ||
                school.type in currentFilters.types

            val matchesRating = school.rating in currentFilters.ratingRange

            val matchesFees = currentFilters.feeRange.isEmpty() ||
                school.annualFees in currentFilters.feeRange

            val matchesFacilities = currentFilters.requiredFacilities.isEmpty() ||
                school.facilities.containsAll(currentFilters.requiredFacilities)

            val matchesPrograms = currentFilters.requiredPrograms.isEmpty() ||
                school.programs.containsAll(currentFilters.requiredPrograms)

            matchesSearch && matchesType && matchesRating &&
                matchesFees && matchesFacilities && matchesPrograms
        }

        _schools.value = when (currentFilters.sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name }
            SortOrder.RATING_ASC -> filtered.sortedBy { it.rating }
            SortOrder.RATING_DESC -> filtered.sortedByDescending { it.rating }
            SortOrder.FEES_ASC -> filtered.sortedBy { it.annualFees }
            SortOrder.FEES_DESC -> filtered.sortedByDescending { it.annualFees }
            SortOrder.DEADLINE_ASC -> filtered.sortedBy { it.applicationDeadline }
            SortOrder.DEADLINE_DESC -> filtered.sortedByDescending { it.applicationDeadline }
        }
    }

    fun toggleViewMode() {
        val newMode = when (_viewMode.value) {
            ViewMode.GRID -> ViewMode.LIST
            ViewMode.LIST -> ViewMode.GRID
        }
        _viewMode.value = newMode
        saveViewMode(newMode)
        trackViewModeChange(newMode)
    }

    fun scrollToTop() {
        viewModelScope.launch {
            listState.scrollToItem(0)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                currentProvinceId?.let { provinceId ->
                    loadSchools(provinceId)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun saveFilters(filters: SchoolFilters) {
        viewModelScope.launch {
            preferencesRepository.setSchoolFilters(filters)
        }
    }

    private fun saveViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            preferencesRepository.setSchoolViewMode(viewMode)
        }
    }

    private fun trackSearch(query: String) {
        if (query.isNotEmpty()) {
            analyticsTracker.trackEvent(
                "school_search",
                mapOf("query_length" to query.length)
            )
        }
    }

    private fun trackFilters(filters: SchoolFilters) {
        analyticsTracker.trackEvent(
            "school_filters_applied",
            mapOf(
                "types" to filters.types.size,
                "has_rating_filter" to (filters.ratingRange != 0f..5f),
                "has_fee_filter" to filters.feeRange.isNotEmpty(),
                "facilities_count" to filters.requiredFacilities.size,
                "programs_count" to filters.requiredPrograms.size
            )
        )
    }

    private fun trackViewModeChange(viewMode: ViewMode) {
        analyticsTracker.trackEvent(
            "school_view_mode_changed",
            mapOf("mode" to viewMode.name)
        )
    }
}

// app/src/main/java/com/example/africanschools/ui/screens/school/models/SchoolModels.kt
data class SchoolUiModel(
    val id: String,
    val name: String,
    val type: SchoolType,
    val description: String,
    val mainImageUrl: String,
    val imageUrls: List<String>,
    val rating: Float,
    val annualFees: Double,
    val facilities: List<String>,
    val programs: List<String>,
    val applicationDeadline: LocalDateTime?,
    val admissionStatus: AdmissionStatus,
    val contact: ContactInfo,
    val location: LocationInfo,
    val statistics: SchoolStatistics
)

data class SchoolFilters(
    val types: Set<SchoolType> = emptySet(),
    val ratingRange: ClosedFloatingPointRange<Float> = 0f..5f,
    val feeRange: ClosedRange<Double> = 0.0..0.0,
    val requiredFacilities: Set<String> = emptySet(),
    val requiredPrograms: Set<String> = emptySet(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC
) {
    val hasActiveFilters: Boolean
        get() = types.isNotEmpty() ||
            ratingRange != 0f..5f ||
            feeRange.start > 0.0 ||
            requiredFacilities.isNotEmpty() ||
            requiredPrograms.isNotEmpty()

    val activeFilterCount: Int
        get() = listOf(
            types.isNotEmpty(),
            ratingRange != 0f..5f,
            feeRange.start > 0.0,
            requiredFacilities.isNotEmpty(),
            requiredPrograms.isNotEmpty()
        ).count { it }
}

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    RATING_ASC,
    RATING_DESC,
    FEES_ASC,
    FEES_DESC,
    DEADLINE_ASC,
    DEADLINE_DESC
}

enum class AdmissionStatus {
    OPEN,
    CLOSING_SOON,
    CLOSED,
    WAITLIST
}

data class ContactInfo(
    val email: String?,
    val phone: String?,
    val website: String?,
    val address: String
)

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val distance: Double?
)

data class SchoolStatistics(
    val studentCount: Int,
    val teacherCount: Int,
    val classSize: Int,
    val graduationRate: Float,
    val acceptanceRate: Float
)
// app/src/main/java/com/example/africanschools/ui/screens/school/components/SchoolFilterSheet.kt
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SchoolFilterSheet(
    currentFilters: SchoolFilters,
    schoolTypes: Set<SchoolType>,
    onFilterChange: (SchoolFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
    var selectedTab by remember { mutableStateOf(0) }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Filter Schools",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Basic") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Advanced") }
                    )
                }

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally() with 
                        fadeOut() + slideOutHorizontally()
                    }
                ) { tab ->
                    when (tab) {
                        0 -> BasicFilters(
                            currentFilters = currentFilters,
                            schoolTypes = schoolTypes,
                            onFilterChange = onFilterChange
                        )
                        1 -> AdvancedFilters(
                            currentFilters = currentFilters,
                            onFilterChange = onFilterChange
                        )
                    }
                }

                FilterActions(
                    hasActiveFilters = currentFilters.hasActiveFilters,
                    onClear = { onFilterChange(SchoolFilters()) },
                    onApply = onDismiss
                )
            }
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = onDismiss
    ) {}
}

@Composable
private fun BasicFilters(
    currentFilters: SchoolFilters,
    schoolTypes: Set<SchoolType>,
    onFilterChange: (SchoolFilters) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // School Type Filter
        FilterSection(title = "School Type") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                schoolTypes.forEach { type ->
                    FilterChip(
                        selected = type in currentFilters.types,
                        onClick = {
                            val newTypes = if (type in currentFilters.types) {
                                currentFilters.types - type
                            } else {
                                currentFilters.types + type
                            }
                            onFilterChange(currentFilters.copy(types = newTypes))
                        },
                        label = { Text(type.displayName) }
                    )
                }
            }
        }

        // Rating Filter
        FilterSection(title = "Minimum Rating") {
            RatingSlider(
                value = currentFilters.ratingRange.start,
                onValueChange = { rating ->
                    onFilterChange(currentFilters.copy(
                        ratingRange = rating..5f
                    ))
                }
            )
        }

        // Fee Range Filter
        FilterSection(title = "Annual Fees") {
            FeeRangeSlider(
                range = currentFilters.feeRange,
                onRangeChange = { range ->
                    onFilterChange(currentFilters.copy(feeRange = range))
                }
            )
        }
    }
}

@Composable
private fun AdvancedFilters(
    currentFilters: SchoolFilters,
    onFilterChange: (SchoolFilters) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Facilities Filter
        FilterSection(title = "Required Facilities") {
            FacilitiesSelector(
                selectedFacilities = currentFilters.requiredFacilities,
                onSelectionChange = { facilities ->
                    onFilterChange(currentFilters.copy(
                        requiredFacilities = facilities
                    ))
                }
            )
        }

        // Programs Filter
        FilterSection(title = "Required Programs") {
            ProgramsSelector(
                selectedPrograms = currentFilters.requiredPrograms,
                onSelectionChange = { programs ->
                    onFilterChange(currentFilters.copy(
                        requiredPrograms = programs
                    ))
                }
            )
        }

        // Additional Filters
        AdmissionStatusFilter(
            currentFilters = currentFilters,
            onFilterChange = onFilterChange
        )

        DistanceFilter(
            currentFilters = currentFilters,
            onFilterChange = onFilterChange
        )
    }
}

@Composable
private fun RatingSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RatingBar(
                rating = value,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.body2
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..5f,
            steps = 9
        )
    }
}

@Composable
private fun FacilitiesSelector(
    selectedFacilities: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    val facilities = remember {
        listOf(
            "Library",
            "Sports Complex",
            "Science Labs",
            "Computer Labs",
            "Auditorium",
            "Cafeteria",
            "Medical Center",
            "Transport",
            "Dormitory"
        )
    }

    SelectableGrid(
        items = facilities,
        selectedItems = selectedFacilities,
        onSelectionChange = onSelectionChange
    )
}

@Composable
private fun SelectableGrid(
    items: List<String>,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item in selectedItems,
                onClick = {
                    val newSelection = if (item in selectedItems) {
                        selectedItems - item
                    } else {
                        selectedItems + item
                    }
                    onSelectionChange(newSelection)
                },
                label = { Text(item) }
            )
        }
    }
}

@Composable
private fun SchoolComparisonSheet(
    schools: List<SchoolUiModel>,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Compare Schools",
                style = MaterialTheme.typography.h6
            )
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(schools) { school ->
                    SchoolComparisonCard(school = school)
                }
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun SchoolComparisonCard(
    school: SchoolUiModel
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = school.mainImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = school.name,
                style = MaterialTheme.typography.h6,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            ComparisonMetric(
                label = "Rating",
                value = school.rating,
                maxValue = 5f
            )

            ComparisonMetric(
                label = "Annual Fees",
                value = school.annualFees,
                format = { "$$it" }
            )

            ComparisonMetric(
                label = "Students",
                value = school.statistics.studentCount
            )

            ComparisonMetric(
                label = "Class Size",
                value = school.statistics.classSize
            )

            ComparisonMetric(
                label = "Graduation Rate",
                value = school.statistics.graduationRate,
                format = { "${it}%" }
            )
        }
    }
}

@Composable
private fun ComparisonMetric(
    label: String,
    value: Number,
    maxValue: Float? = null,
    format: ((Number) -> String)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption
        )
        
        if (maxValue != null) {
            LinearProgressIndicator(
                progress = (value.toFloat() / maxValue),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Text(
            text = format?.invoke(value) ?: value.toString(),
            style = MaterialTheme.typography.body1
        )
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/SchoolDetailScreen.kt
@Composable
fun SchoolDetailScreen(
    schoolId: String,
    onNavigateUp: () -> Unit,
    viewModel: SchoolDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val school by viewModel.school.collectAsState()
    var showApplicationForm by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var showVirtualTour by remember { mutableStateOf(false) }

    LaunchedEffect(schoolId) {
        viewModel.loadSchoolDetails(schoolId)
    }

    Scaffold(
        topBar = {
            SchoolDetailTopBar(
                school = school,
                onNavigateUp = onNavigateUp,
                onShare = viewModel::shareSchool,
                onFavorite = viewModel::toggleFavorite
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState is UiState.Loading -> LoadingIndicator()
                uiState is UiState.Error -> ErrorState(
                    message = (uiState as UiState.Error).message,
                    onRetry = { viewModel.loadSchoolDetails(schoolId) }
                )
                school != null -> {
                    SchoolDetailContent(
                        school = school!!,
                        onApplyClick = { showApplicationForm = true },
                        onGalleryClick = { showGallery = true },
                        onVirtualTourClick = { showVirtualTour = true }
                    )
                }
            }
        }
    }

    // Dialogs and bottom sheets
    if (showApplicationForm) {
        ApplicationFormDialog(
            school = school!!,
            onDismiss = { showApplicationForm = false },
            onSubmit = viewModel::submitApplication
        )
    }

    if (showGallery) {
        SchoolGallery(
            images = school!!.imageUrls,
            onDismiss = { showGallery = false }
        )
    }

    if (showVirtualTour) {
        VirtualTourDialog(
            tourUrl = school!!.virtualTourUrl,
            onDismiss = { showVirtualTour = false }
        )
    }
}

@Composable
private fun SchoolDetailContent(
    school: SchoolDetailUiModel,
    onApplyClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onVirtualTourClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header Section with Image Carousel
        SchoolImageCarousel(
            images = school.imageUrls,
            onGalleryClick = onGalleryClick
        )

        // Main Info Section
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SchoolHeaderInfo(
                school = school,
                onApplyClick = onApplyClick,
                onVirtualTourClick = onVirtualTourClick
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Tab Layout for different sections
            TabRow(selectedTabIndex = selectedTab) {
                SchoolDetailTabs.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() with fadeOut()
                }
            ) { tab ->
                when (SchoolDetailTabs.values()[tab]) {
                    SchoolDetailTabs.OVERVIEW -> SchoolOverview(school)
                    SchoolDetailTabs.ACADEMICS -> SchoolAcademics(school)
                    SchoolDetailTabs.ADMISSIONS -> SchoolAdmissions(school)
                    SchoolDetailTabs.FACILITIES -> SchoolFacilities(school)
                }
            }
        }
    }
}

@Composable
private fun SchoolHeaderInfo(
    school: SchoolDetailUiModel,
    onApplyClick: () -> Unit,
    onVirtualTourClick: () -> Unit
) {
    Column {
        Text(
            text = school.name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SchoolTypeChip(type = school.type)
            RatingDisplay(rating = school.rating)
        }

        Text(
            text = school.description,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onApplyClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Now")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onVirtualTourClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Virtual Tour")
            }
        }

        if (school.admissionStatus == AdmissionStatus.CLOSING_SOON) {
            DeadlineWarning(deadline = school.applicationDeadline)
        }
    }
}

@Composable
private fun SchoolOverview(school: SchoolDetailUiModel) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Key Statistics
        StatisticsGrid(statistics = school.statistics)

        // Contact Information
        ContactSection(contact = school.contact)

        // Location
        LocationSection(
            location = school.location,
            onDirectionsClick = { /* Handle directions */ }
        )

        // Awards and Recognitions
        if (school.awards.isNotEmpty()) {
            AwardsSection(awards = school.awards)
        }
    }
}

@Composable
private fun SchoolAcademics(school: SchoolDetailUiModel) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Programs Offered
        ProgramsSection(programs = school.programs)

        // Academic Performance
        AcademicPerformanceSection(performance = school.academicPerformance)

        // Faculty Information
        FacultySection(faculty = school.faculty)

        // Extracurricular Activities
        ExtracurricularSection(activities = school.extracurricular)
    }
}

@Composable
private fun SchoolAdmissions(school: SchoolDetailUiModel) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Admission Requirements
        RequirementsSection(requirements = school.admissionRequirements)

        // Application Process
        ApplicationProcessSection(process = school.applicationProcess)

        // Fees and Financial Aid
        FeesSection(
            fees = school.fees,
            financialAid = school.financialAid
        )

        // Important Dates
        DatesSection(dates = school.importantDates)
    }
}

@Composable
private fun SchoolFacilities(school: SchoolDetailUiModel) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Facilities List with Images
        FacilitiesGrid(facilities = school.facilities)

        // Infrastructure Details
        InfrastructureSection(infrastructure = school.infrastructure)

        // Special Features
        SpecialFeaturesSection(features = school.specialFeatures)
    }
}

enum class SchoolDetailTabs(val title: String) {
    OVERVIEW("Overview"),
    ACADEMICS("Academics"),
    ADMISSIONS("Admissions"),
    FACILITIES("Facilities")
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/SchoolDetailViewModel.kt
package com.example.africanschools.ui.screens.school.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.africanschools.data.model.Resource
import com.example.africanschools.domain.usecase.GetSchoolDetailsUseCase
import com.example.africanschools.ui.base.BaseViewModel
import com.example.africanschools.ui.common.UiEvent
import com.example.africanschools.ui.screens.school.models.SchoolDetailUiModel
import com.example.africanschools.ui.screens.school.models.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchoolDetailViewModel @Inject constructor(
    private val getSchoolDetailsUseCase: GetSchoolDetailsUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _school = MutableStateFlow<SchoolDetailUiModel?>(null)
    val school: StateFlow<SchoolDetailUiModel?> = _school.asStateFlow()

    private val schoolId: String = savedStateHandle["schoolId"] ?: ""

    init {
        loadSchoolDetails(schoolId)
    }

    fun loadSchoolDetails(schoolId: String) {
        launchWithState {
            getSchoolDetailsUseCase(schoolId)
                .collectResource { schoolDetails ->
                    _school.value = schoolDetails.toUiModel()
                }
        }
    }

    fun submitApplication(applicationData: ApplicationData) {
        // Implement application submission logic, e.g., make a network request
        viewModelScope.launch {
            // Simulate submission
            sendEvent(UiEvent.ShowMessage("Application submitted successfully!"))
        }
    }

    fun shareSchool() {
        school.value?.let {
            val shareText = "Check out ${it.name} on African Schools App!"
            // Implement share logic, e.g., using a Share Intent
            sendEvent(UiEvent.ShareContent(shareText))
        }
    }

    fun toggleFavorite() {
        // Implement favorite toggle logic
        viewModelScope.launch {
            school.value?.let {
                val isFavorite = !it.isFavorite
                _school.value = it.copy(isFavorite = isFavorite)
                // Save to database or shared preferences
                sendEvent(
                    UiEvent.ShowMessage(
                        if (isFavorite) "${it.name} added to favorites"
                        else "${it.name} removed from favorites"
                    )
                )
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/models/SchoolDetailUiModel.kt
package com.example.africanschools.ui.screens.school.models

import com.example.africanschools.data.model.*

data class SchoolDetailUiModel(
    val id: String,
    val name: String,
    val type: SchoolType,
    val description: String,
    val imageUrls: List<String>,
    val mainImageUrl: String,
    val rating: Float,
    val admissionStatus: AdmissionStatus,
    val applicationDeadline: String?,
    val virtualTourUrl: String?,
    val isFavorite: Boolean,
    val statistics: SchoolStatistics,
    val contact: ContactInfo,
    val location: LocationInfo,
    val programs: List<Program>,
    val facilities: List<String>,
    val admissionRequirements: List<String>,
    val applicationProcess: List<String>,
    val fees: Fees,
    val financialAid: FinancialAid,
    val academicPerformance: AcademicPerformance,
    val faculty: List<FacultyMember>,
    val extracurricular: List<String>,
    val awards: List<String>,
    val infrastructure: Infrastructure,
    val specialFeatures: List<String>,
    val importantDates: List<ImportantDate>
)

fun SchoolDetails.toUiModel() = SchoolDetailUiModel(
    id = id,
    name = name,
    type = type,
    description = description,
    imageUrls = imageUrls,
    mainImageUrl = imageUrls.firstOrNull() ?: "",
    rating = calculateRating(),
    admissionStatus = determineAdmissionStatus(),
    applicationDeadline = formatApplicationDeadline(),
    virtualTourUrl = virtualTourUrl,
    isFavorite = isFavorite(),
    statistics = statistics,
    contact = contact,
    location = location,
    programs = programs,
    facilities = facilities,
    admissionRequirements = admissionRequirements,
    applicationProcess = applicationProcess,
    fees = fees,
    financialAid = financialAid,
    academicPerformance = academicPerformance,
    faculty = faculty,
    extracurricular = extracurricularActivities,
    awards = awards,
    infrastructure = infrastructure,
    specialFeatures = specialFeatures,
    importantDates = importantDates
)

// Additional data classes
data class Program(val name: String, val description: String)
data class Fees(val tuition: Double, val otherFees: Double)
data class FinancialAid(val scholarships: List<String>, val grants: List<String>)
data class AcademicPerformance(val graduationRate: Float, val averageGPA: Float)
data class FacultyMember(val name: String, val position: String, val qualifications: String)
data class Infrastructure(val buildings: Int, val classrooms: Int, val laboratories: Int)
data class ImportantDate(val event: String, val date: String)
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/components/ApplicationFormDialog.kt
@Composable
fun ApplicationFormDialog(
    school: SchoolDetailUiModel,
    onDismiss: () -> Unit,
    onSubmit: (ApplicationData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply to ${school.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val applicationData = ApplicationData(
                        name = name,
                        email = email,
                        phone = phone,
                        message = message,
                        schoolId = school.id
                    )
                    onSubmit(applicationData)
                    onDismiss()
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data class for application data
data class ApplicationData(
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val schoolId: String
)
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/components/SchoolGallery.kt
@Composable
fun SchoolGallery(
    images: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val pagerState = rememberPagerState()
            HorizontalPager(
                count = images.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/components/VirtualTourDialog.kt
@Composable
fun VirtualTourDialog(
    tourUrl: String?,
    onDismiss: () -> Unit
) {
    if (tourUrl == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Virtual Tour Unavailable") },
            text = { Text("This school does not have a virtual tour available.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Box(modifier = Modifier.fillMaxSize()) {
                // WebView or video player for virtual tour
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            loadUrl(tourUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
@Composable
private fun ContactSection(contact: ContactInfo) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Contact Information", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        if (contact.email != null) {
            ClickableText(
                text = AnnotatedString("Email: ${contact.email}"),
                onClick = { /* Handle email intent */ },
                style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.primary)
            )
        }
        if (contact.phone != null) {
            ClickableText(
                text = AnnotatedString("Phone: ${contact.phone}"),
                onClick = { /* Handle phone intent */ },
                style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.primary)
            )
        }
        if (contact.website != null) {
            ClickableText(
                text = AnnotatedString("Website: ${contact.website}"),
                onClick = { /* Handle web intent */ },
                style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.primary)
            )
        }
        Text("Address: ${contact.address}", style = MaterialTheme.typography.body2)
    }
}

@Composable
private fun LocationSection(location: LocationInfo, onDirectionsClick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Location", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        MapView(
            latitude = location.latitude,
            longitude = location.longitude,
            onMapClick = onDirectionsClick
        )
    }
}

@Composable
private fun MapView(latitude: Double, longitude: Double, onMapClick: () -> Unit) {
    val position = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(position, 14f)
    }
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onMapClick),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = position),
            title = "School Location"
        )
    }
}
@Composable
private fun FacilitiesGrid(facilities: List<String>) {
    if (facilities.isEmpty()) {
        Text("Facilities information not available.")
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            facilities.forEach { facility ->
                Chip(
                    onClick = { /* Handle facility click */ },
                    label = { Text(facility) }
                )
            }
        }
    }
}
// In SchoolDetailScreen.kt
LaunchedEffect(Unit) {
    viewModel.uiEvent.collect { event ->
        when (event) {
            is UiEvent.ShowMessage -> {
                // Show Snackbar or Toast
                ScaffoldState.snackbarHostState.showSnackbar(event.message)
            }
            is UiEvent.ShareContent -> {
                // Trigger share intent
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
            else -> Unit
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/SearchScreen.kt
package com.example.africanschools.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.africanschools.ui.screens.common.SchoolListItem
import com.example.africanschools.ui.screens.search.components.SearchFilterDialog
import com.example.africanschools.ui.screens.search.components.SortOrderDialog

@Composable
fun SearchScreen(
    onSchoolClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFiltering by viewModel.isFiltering.collectAsState()
    val isSorting by viewModel.isSorting.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onNavigateUp = onNavigateUp,
                onFilterClick = { showFilterDialog = true },
                onSortClick = { showSortDialog = true }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> LoadingIndicator()
                uiState is UiState.Error -> ErrorState(
                    message = (uiState as UiState.Error).message,
                    onRetry = viewModel::retrySearch
                )
                searchResults.isEmpty() -> EmptyState(message = "No results found.")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        itemsIndexed(searchResults) { index, school ->
                            SchoolListItem(
                                school = school,
                                onClick = { onSchoolClick(school.id) }
                            )
                            if (index < searchResults.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }
            }
            if (showFilterDialog) {
                SearchFilterDialog(
                    currentFilters = viewModel.currentFilters,
                    onApplyFilters = {
                        viewModel.applyFilters(it)
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
            if (showSortDialog) {
                SortOrderDialog(
                    currentSortOrder = viewModel.currentSortOrder,
                    onSortOrderSelected = {
                        viewModel.setSortOrder(it)
                        showSortDialog = false
                    },
                    onDismiss = { showSortDialog = false }
                )
            }
        }
    }
}

@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search schools...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                keyboardActions = KeyboardActions(onSearch = { /* Trigger search */ }),
                imeAction = ImeAction.Search,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    focusedIndicatorColor = MaterialTheme.colors.primary
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
            IconButton(onClick = onSortClick) {
                Icon(Icons.Default.Sort, contentDescription = "Sort")
            }
        }
    )
}

// app/src/main/java/com/example/africanschools/ui/screens/search/SearchViewModel.kt
package com.example.africanschools.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.example.africanschools.data.model.*
import com.example.africanschools.domain.usecase.SearchSchoolsUseCase
import com.example.africanschools.ui.base.BaseViewModel
import com.example.africanschools.ui.base.UiState
import com.example.africanschools.ui.screens.school.models.SchoolUiModel
import com.example.africanschools.ui.screens.school.models.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSchoolsUseCase: SearchSchoolsUseCase
) : BaseViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SchoolUiModel>>(emptyList())
    val searchResults: StateFlow<List<SchoolUiModel>> = _searchResults.asStateFlow()

    val isLoading: StateFlow<Boolean> = uiState.map { it is UiState.Loading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    var currentFilters: SearchFilters = SearchFilters()
        private set

    var currentSortOrder: SortOrder = SortOrder.NAME_ASC
        private set

    private var searchJob: Job? = null

    init {
        observeSearchQuery()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.length >= 2 || it.isEmpty() }
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            uiState.value = UiState.Loading
            searchSchoolsUseCase(query, currentFilters, currentSortOrder)
                .catch { error ->
                    uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
                .collect { schools ->
                    _searchResults.value = schools.map { it.toUiModel() }
                    uiState.value = UiState.Success
                }
        }
    }

    fun applyFilters(filters: SearchFilters) {
        currentFilters = filters
        performSearch(_searchQuery.value)
    }

    fun setSortOrder(sortOrder: SortOrder) {
        currentSortOrder = sortOrder
        performSearch(_searchQuery.value)
    }

    fun retrySearch() {
        performSearch(_searchQuery.value)
    }
}
// app/src/main/java/com/example/africanschools/data/model/SearchFilters.kt
package com.example.africanschools.data.model

data class SearchFilters(
    val countryId: String? = null,
    val provinceId: String? = null,
    val schoolType: Set<SchoolType> = emptySet(),
    val ratingRange: ClosedFloatingPointRange<Float> = 0f..5f // Example filter
    // Add more filters as needed
)

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    RATING_ASC,
    RATING_DESC,
    // Add more sort options as needed
}// app/src/main/java/com/example/africanschools/data/repository/MainRepository.kt
// ...

class MainRepository @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiService: ApiService
) {
    // Existing methods...

    fun searchSchools(
        query: String,
        filters: SearchFilters,
        sortOrder: SortOrder
    ): Flow<List<School>> = flow {
        // Fetch from API
        val response = apiService.searchSchools(
            query = query,
            countryId = filters.countryId,
            provinceId = filters.provinceId,
            schoolType = filters.schoolType.firstOrNull(), // Simplified for example
            language = "en" // Use current language
        )
        // Save results to database
        databaseRepository.updateSchools(response.items)
        // Emit from database
        val schools = databaseRepository.searchSchoolsFromDb(query, filters)
        // Apply sorting
        val sortedSchools = when (sortOrder) {
            SortOrder.NAME_ASC -> schools.sortedBy { it.name }
            SortOrder.NAME_DESC -> schools.sortedByDescending { it.name }
            SortOrder.RATING_ASC -> schools.sortedBy { it.rating }
            SortOrder.RATING_DESC -> schools.sortedByDescending { it.rating }
        }
        emit(sortedSchools)
    }
}
// app/src/main/java/com/example/africanschools/domain/usecase/SearchSchoolsUseCase.kt
package com.example.africanschools.domain.usecase

import com.example.africanschools.data.model.*
import com.example.africanschools.data.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchSchoolsUseCase @Inject constructor(
    private val repository: MainRepository
) {
    operator fun invoke(
        query: String,
        filters: SearchFilters,
        sortOrder: SortOrder
    ): Flow<List<School>> {
        return repository.searchSchools(query, filters, sortOrder)
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/SearchFilterDialog.kt
package com.example.africanschools.ui.screens.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.africanschools.data.model.*
import com.example.africanschools.ui.components.RangeSliderWithLabel

@Composable
fun SearchFilterDialog(
    currentFilters: SearchFilters,
    onApplyFilters: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSchoolTypes by remember { mutableStateOf(currentFilters.schoolType) }
    var ratingRange by remember { mutableStateOf(currentFilters.ratingRange) }
    // Add more filter states as needed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Schools") },
        text = {
            Column {
                // School Type Filter
                Text("School Type")
                SchoolTypeSelector(
                    selectedTypes = selectedSchoolTypes,
                    onSelectionChange = { selectedSchoolTypes = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating Range Filter
                Text("Minimum Rating")
                RangeSliderWithLabel(
                    valueRange = 0f..5f,
                    values = ratingRange,
                    onValueChange = { ratingRange = it }
                )

                // Add more filters as needed
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newFilters = currentFilters.copy(
                    schoolType = selectedSchoolTypes,
                    ratingRange = ratingRange
                    // Set other filters
                )
                onApplyFilters(newFilters)
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SchoolTypeSelector(
    selectedTypes: Set<SchoolType>,
    onSelectionChange: (Set<SchoolType>) -> Unit
) {
    val allTypes = SchoolType.values()
    Column {
        allTypes.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = type in selectedTypes,
                    onCheckedChange = {
                        val newSelection = if (it) {
                            selectedTypes + type
                        } else {
                            selectedTypes - type
                        }
                        onSelectionChange(newSelection)
                    }
                )
                Text(type.name.capitalize())
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/SortOrderDialog.kt
package com.example.africanschools.ui.screens.search.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import com.example.africanschools.data.model.SortOrder

@Composable
fun SortOrderDialog(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    val sortOptions = SortOrder.values()
    var selectedSort by remember { mutableStateOf(currentSortOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                sortOptions.forEach { sortOrder ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedSort == sortOrder,
                            onClick = { selectedSort = sortOrder }
                        )
                        Text(sortOrder.name.replace("_", " ").capitalize())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSortOrderSelected(selectedSort) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/RangeSliderWithLabel.kt
@Composable
fun RangeSliderWithLabel(
    valueRange: ClosedFloatingPointRange<Float>,
    values: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val (start, end) = values
    Column {
        Text("From $start to $end")
        Slider(
            value = start,
            onValueChange = { onValueChange(it..end) },
            valueRange = valueRange,
            steps = 10,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Slider(
            value = end,
            onValueChange = { onValueChange(start..it) },
            valueRange = valueRange,
            steps = 10,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/components/StatisticsGrid.kt
@Composable
private fun StatisticsGrid(statistics: SchoolStatistics) {
    Column {
        Text("Key Statistics", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(label = "Students", value = statistics.studentCount)
            StatisticItem(label = "Class Size", value = statistics.classSize)
            StatisticItem(label = "Teachers", value = statistics.teacherCount)
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: Number) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value.toString(), style = MaterialTheme.typography.h6)
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/components/AwardsSection.kt
@Composable
private fun AwardsSection(awards: List<String>) {
    Column {
        Text("Awards & Recognitions", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        awards.forEach { award ->
            Text("• $award", style = MaterialTheme.typography.body2
// app/src/main/java/com/example/africanschools/ui/screens/search/components/RangeSliderWithLabel.kt
@Composable
fun RangeSliderWithLabel(
    valueRange: ClosedFloatingPointRange<Float>,
    values: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val (start, end) = values
    Column {
        RangeSlider(
            valueRange = valueRange,
            values = values,
            onValueChange = onValueChange
        )
        Row {
            Text("From: $start")
            Spacer(modifier = Modifier.weight(1f))
            Text("To: $end")
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/RangeSlider.kt
@Composable
fun RangeSlider(
    valueRange: ClosedFloatingPointRange<Float>,
    values: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val (start, end) = values

    Slider(
        value = start,
        onValueChange = { onValueChange(it..end) },
        valueRange = valueRange,
        steps = 0,
        modifier = Modifier.fillMaxWidth()
    )
    Slider(
        value = end,
        onValueChange = { onValueChange(start..it) },
        valueRange = valueRange,
        steps = 0,
        modifier = Modifier.fillMaxWidth()
    )
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/SchoolTypeSelector.kt
@Composable
fun SchoolTypeSelector(
    selectedTypes: Set<SchoolType>,
    onSelectionChange: (Set<SchoolType>) -> Unit
) {
    val allTypes = SchoolType.values()
    Column {
        allTypes.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = type in selectedTypes,
                    onCheckedChange = {
                        val newSelection = if (it) {
                            selectedTypes + type
                        } else {
                            selectedTypes - type
                        }
                        onSelectionChange(newSelection)
                    }
                )
                Text(type.name.capitalize())
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/SearchFilterDialog.kt
@Composable
fun SearchFilterDialog(
// Update the NavGraph.kt file
composable(NavDestination.Search.route) {
    SearchScreen(
        onSchoolClick = navigationActions.navigateToSchoolDetail,
        onNavigateUp = navigationActions.navigateUp
    )
}
// Ensure navigateToSearch is properly defined
val navigateToSearch: () -> Unit = {
    navController.navigate(NavDestination.Search.route)
}
// In CountryListScreen.kt's top bar
IconButton(onClick = onSearchClick) {
    Icon(Icons.Default.Search, contentDescription = "Search")
}
// app/src/main/java/com/example/africanschools/data/model/FilterCriteria.kt
package com.example.africanschools.data.model

data class FilterCriteria(
    val countryId: String? = null,
    val provinceId: String? = null,
    val schoolTypes: Set<SchoolType> = emptySet(),
    val ratingRange: ClosedFloatingPointRange<Float> = 0f..5f,
    val feesRange: ClosedFloatingPointRange<Double> = 0.0..100000.0,
    val facilities: Set<String> = emptySet(),
    val programs: Set<String> = emptySet(),
    val admissionStatuses: Set<AdmissionStatus> = emptySet()
    // Add more filters as needed
)
// app/src/main/java/com/example/africanschools/ui/components/filters/FilterSection.kt
package com.example.africanschools.ui.components.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical: 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom: 4.dp)
        )
        content()
    }
}
// app/src/main/java/com/example/africanschools/ui/components/filters/FilterSection.kt
package com.example.africanschools.ui.components.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical: 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom: 4.dp)
        )
        content()
    }
}
// app/src/main/java/com/example/africanschools/ui/components/filters/RatingRangeSlider.kt
package com.example.africanschools.ui.components.filters

import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun RatingRangeSlider(
    ratingRange: ClosedFloatingPointRange<Float>,
    onRatingRangeChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    var sliderPosition by remember { mutableStateOf(ratingRange) }

    FilterSection(title = "Minimum Rating") {
        Slider(
            value = sliderPosition.start,
            onValueChange = {
                sliderPosition = it..5f
                onRatingRangeChange(sliderPosition)
            },
            valueRange = 0f..5f,
            steps = 4
        )
        Text(text = "From ${sliderPosition.start} Stars")
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/search/SearchViewModel.kt
package com.example.africanschools.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.example.africanschools.data.model.*
import com.example.africanschools.domain.usecase.SearchSchoolsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel @Inject constructor(
    private val searchSchoolsUseCase: SearchSchoolsUseCase
) : BaseViewModel() {

    // Existing properties
    private val _filters = MutableStateFlow(FilterCriteria())
    val filters: StateFlow<FilterCriteria> = _filters.asStateFlow()

    fun setFilters(newFilters: FilterCriteria) {
        _filters.value = newFilters
        performSearch(_searchQuery.value)
    }

    // Modify performSearch to accept filters
    fun performSearch(query: String) {
        // ...
        searchSchoolsUseCase(query, _filters.value, currentSortOrder)
        // ...
    }
}

// app/src/main/java/com/example/africanschools/data/repository/MainRepository.kt
package com.example.africanschools.data.repository

class MainRepository @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiService: ApiService
) {
    // Existing methods...

    fun searchSchools(
        query: String,
        filters: FilterCriteria,
        sortOrder: SortOrder
    ): Flow<List<School>> = flow {
        // Use filters in API call if applicable
        val response = apiService.searchSchools(
            query = query,
            countryId = filters.countryId,
            provinceId = filters.provinceId,
            schoolType = filters.schoolTypes.firstOrNull(),
            ratingMin = filters.ratingRange.start,
            ratingMax = filters.ratingRange.endInclusive,
            // Include other filters
        )
        // Save results to database
        databaseRepository.updateSchools(response.items)
        // Emit from database applying filters locally if necessary
        val schools = databaseRepository.searchSchoolsFromDb(query, filters)
        // Apply sorting
        // ...
        emit(sortedSchools)
    }
}

// app/src/main/java/com/example/africanschools/data/dao/SchoolDao.kt
@Dao
interface SchoolDao {
    @Query("""
        SELECT * FROM schools
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:schoolType IS NULL OR type IN (:schoolTypes))
        AND rating BETWEEN :ratingMin AND :ratingMax
        AND fees BETWEEN :feesMin AND :feesMax
        -- Add conditions for facilities, programs, admission statuses
    """)
    fun searchSchoolsWithFilters(
        query: String,
        schoolTypes: Set<SchoolType>?,
        ratingMin: Float,
        ratingMax: Float,
        feesMin: Double,
        feesMax: Double
        // Other parameters
    ): Flow<List<School>>
}
IconButton(onClick = { showFilterDialog = true }) {
    Icon(Icons.Default.FilterList, contentDescription = "Filter")
}
// app/src/main/java/com/example/africanschools/ui/screens/search/components/FilterDialog.kt
package com.example.africanschools.ui.screens.search.components

@Composable
fun FilterDialog(
    currentFilters: FilterCriteria,
    onFiltersChanged: (FilterCriteria) -> Unit,
    onDismiss: () -> Unit
) {
    // Use the filter components created earlier
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Schools") },
        text = {
            Column {
                SchoolTypeSelector(
                    selectedTypes = currentFilters.schoolTypes,
                    onSelectionChange = { types ->
                        onFiltersChanged(currentFilters.copy(schoolTypes = types))
                    }
                )
                RatingRangeSlider(
                    ratingRange = currentFilters.ratingRange,
                    onRatingRangeChange = { range ->
                        onFiltersChanged(currentFilters.copy(ratingRange = range))
                    }
                )
                // Add additional filter components
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
// app/src/main/java/com/example/africanschools/data/model/School.kt

@Entity(
    tableName = "schools",
    foreignKeys = [
        ForeignKey(
            entity = Province::class,
            parentColumns = ["id"],
            childColumns = ["provinceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class School(
    @PrimaryKey val id: String,
    // Existing fields...
    val isFavorite: Boolean = false, // New field
    val lastUpdated: Long = System.currentTimeMillis()
)
// app/src/main/java/com/example/africanschools/data/dao/SchoolDao.kt

@Dao
interface SchoolDao {
    // Existing methods...

    @Query("SELECT * FROM schools WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteSchools(): Flow<List<School>>

    @Update
    suspend fun updateSchool(school: School)
}
// app/src/main/java/com/example/africanschools/data/database/AfricanSchoolsDatabase.kt

@Database(
    entities = [
        Country::class,
        Province::class,
        School::class,
        Translation::class
    ],
    version = 2, // Increment the version
    exportSchema = true
)
abstract class AfricanSchoolsDatabase : RoomDatabase() {
    // Existing code...
}
// app/src/main/java/com/example/africanschools/data/database/DatabaseMigrations.kt

object DatabaseMigrations {
    // Migration from version 1 to 2
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add the isFavorite column with default value false (0)
            database.execSQL("ALTER TABLE schools ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2
        // Add future migrations here
    )
}
// In AfricanSchoolsDatabase companion object
private fun buildDatabase(context: Context): AfricanSchoolsDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AfricanSchoolsDatabase::class.java,
        DATABASE_NAME
    )
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .build()
}
// app/src/main/java/com/example/africanschools/data/repository/DatabaseRepository.kt

class DatabaseRepository @Inject constructor(
    private val database: AfricanSchoolsDatabase
) {
    private val schoolDao = database.schoolDao()
    // Existing code...

    suspend fun toggleFavorite(schoolId: String) {
        val school = schoolDao.getSchoolById(schoolId)
        if (school != null) {
            val updatedSchool = school.copy(isFavorite = !school.isFavorite)
            schoolDao.updateSchool(updatedSchool)
        }
    }

    fun getFavoriteSchools(): Flow<List<SchoolWithTranslation>> {
        return schoolDao.getFavoriteSchools()
            .combine(
                translationDao.getAllTranslations(EntityType.SCHOOL)
            ) { schools, translations ->
                schools.map { school ->
                    val translation = translations.find { it.entityId == school.id }
                    SchoolWithTranslation(
                        school = school,
                        translatedName = translation?.name ?: school.name,
                        translatedDescription = translation?.description ?: school.description,
                        translatedPrograms = translation?.additionalInfo?.get("programs")
                            ?.split(",") ?: school.programs,
                        translatedRequirements = translation?.additionalInfo?.get("requirements")
                            ?.split(",") ?: school.admissionRequirements
                    )
                }
            }
    }
}
// app/src/main/java/com/example/africanschools/repository/MainRepository.kt

class MainRepository @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiService: ApiService
) {
    // Existing methods...

    // Favorites
    fun getFavoriteSchools(): Flow<List<SchoolWithTranslation>> {
        return databaseRepository.getFavoriteSchools()
    }

    suspend fun toggleFavorite(schoolId: String) {
        databaseRepository.toggleFavorite(schoolId)
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/SchoolDetailViewModel.kt

@HiltViewModel
class SchoolDetailViewModel @Inject constructor(
    private val getSchoolDetailsUseCase: GetSchoolDetailsUseCase,
    private val mainRepository: MainRepository,
    //...
) : BaseViewModel() {
    // Existing code...

    fun toggleFavorite() {
        viewModelScope.launch {
            school.value?.let {
                mainRepository.toggleFavorite(it.id)
                // Update the local school object
                val updatedSchool = it.copy(isFavorite = !it.isFavorite)
                _school.value = updatedSchool
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/SchoolListViewModel.kt

@HiltViewModel
class SchoolListViewModel @Inject constructor(
    //...
    private val mainRepository: MainRepository
) : BaseViewModel() {
    // Existing code...

    fun toggleFavorite(schoolId: String) {
        viewModelScope.launch {
            mainRepository.toggleFavorite(schoolId)
            // Update the local list
            _schools.value = _schools.value.map { school ->
                if (school.id == schoolId) {
                    school.copy(isFavorite = !school.isFavorite)
                } else {
                    school
                }
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/common/SchoolListItem.kt

@Composable
fun SchoolListItem(
    school: SchoolUiModel,
    onClick: () -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            // Existing code...

            IconButton(
                onClick = { onFavoriteClick(school.id) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = if (school.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (school.isFavorite) {
                        "Remove from favorites"
                    } else {
                        "Add to favorites"
                    }
                )
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/school/detail/SchoolDetailScreen.kt

@Composable
fun SchoolDetailTopBar(
    school: SchoolDetailUiModel?,
    onNavigateUp: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit
) {
    TopAppBar(
        title = { Text(school?.name ?: "") },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = if (school?.isFavorite == true) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (school?.isFavorite == true) {
                        "Remove from favorites"
                    } else {
                        "Add to favorites"
                    }
                )
            }
        }
    )
}
SchoolDetailTopBar(
    school = school,
    onNavigateUp = onNavigateUp,
    onShare = viewModel::shareSchool,
    onFavorite = viewModel::toggleFavorite
)
// app/src/main/java/com/example/africanschools/ui/screens/favorites/FavoritesScreen.kt

@Composable
fun FavoritesScreen(
    onNavigateUp: () -> Unit,
    onSchoolClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoriteSchools by viewModel.favoriteSchools.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> LoadingIndicator()
                favoriteSchools.isEmpty() -> EmptyState(message = "No favorite schools.")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(favoriteSchools) { school ->
                            SchoolListItem(
                                school = school,
                                onClick = { onSchoolClick(school.id) },
                                onFavoriteClick = viewModel::toggleFavorite
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/favorites/FavoritesViewModel.kt

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : BaseViewModel() {
    private val _favoriteSchools = MutableStateFlow<List<SchoolUiModel>>(emptyList())
    val favoriteSchools: StateFlow<List<SchoolUiModel>> = _favoriteSchools.asStateFlow()

    val isLoading: StateFlow<Boolean> = uiState.map { it is UiState.Loading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        launchWithState {
            mainRepository.getFavoriteSchools()
                .collect { schools ->
                    _favoriteSchools.value = schools.map { it.toUiModel() }
                }
        }
    }

    fun toggleFavorite(schoolId: String) {
        viewModelScope.launch {
            mainRepository.toggleFavorite(schoolId)
            // Update the local list
            _favoriteSchools.value = _favoriteSchools.value.map { school ->
                if (school.id == schoolId) {
                    school.copy(isFavorite = !school.isFavorite)
                } else {
                    school
                }
            }
        }
    }
}
object Favorites : NavDestination("favorites")
composable(NavDestination.Favorites.route) {
    FavoritesScreen(
        onNavigateUp = navigationActions.navigateUp,
        onSchoolClick = navigationActions.navigateToSchoolDetail
    )
}
val navigateToFavorites: () -> Unit = {
    navController.navigate(NavDestination.Favorites.route)
}
IconButton(onClick = onFavoritesClick) {
    Icon(Icons.Default.Favorite, contentDescription = "Favorites")
}
// app/src/main/java/com/example/africanschools/ui/screens/school/models/SchoolUiModel.kt

data class SchoolUiModel(
    val id: String,
    val name: String,
    // Existing fields...
    val isFavorite: Boolean
)

fun SchoolWithTranslation.toUiModel() = SchoolUiModel(
    id = school.id,
    name = translatedName,
    // Map other fields...
    isFavorite = school.isFavorite
)
// app/src/main/java/com/example/africanschools/data/repository/PreferencesRepository.kt

package com.example.africanschools.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.africanschools.data.model.AfricanLanguage
import com.example.africanschools.ui.theme.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var language: AfricanLanguage
        get() {
            val name = sharedPreferences.getString(KEY_LANGUAGE, AfricanLanguage.ENGLISH.name)
            return AfricanLanguage.valueOf(name ?: AfricanLanguage.ENGLISH.name)
        }
        set(value) {
            sharedPreferences.edit {
                putString(KEY_LANGUAGE, value.name)
            }
        }

    var themePreference: ThemePreference
        get() {
            val name = sharedPreferences.getString(KEY_THEME, ThemePreference.SYSTEM.name)
            return ThemePreference.valueOf(name ?: ThemePreference.SYSTEM.name)
        }
        set(value) {
            sharedPreferences.edit {
                putString(KEY_THEME, value.name)
            }
        }
}
// app/src/main/java/com/example/africanschools/di/AppModule.kt

package com.example.africanschools.di

import android.content.Context
import com.example.africanschools.data.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Existing @Module annotation
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Existing provides methods...

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/settings/SettingsScreen.kt

package com.example.africanschools.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.africanschools.data.model.AfricanLanguage
import com.example.africanschools.ui.theme.ThemePreference
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val language by viewModel.language.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Language Selection
            Text(
                text = "Language",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            LanguagePreference(
                currentLanguage = language,
                onLanguageSelected = viewModel::setLanguage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Selection
            Text(
                text = "Theme",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            ThemePreferenceSelector(
                currentTheme = themePreference,
                onThemeSelected = viewModel::setThemePreference
            )
        }
    }
}

@Composable
fun LanguagePreference(
    currentLanguage: AfricanLanguage,
    onLanguageSelected: (AfricanLanguage) -> Unit
) {
    Column {
        AfricanLanguage.values().forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = language == currentLanguage,
                        onValueChange = {
                            onLanguageSelected(language)
                        }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = language == currentLanguage,
                    onClick = null // null recommended for accessibility with toggleable
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = language.name.capitalize())
            }
        }
    }
}

@Composable
fun ThemePreferenceSelector(
    currentTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column {
        ThemePreference.values().forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = theme == currentTheme,
                        onValueChange = {
                            onThemeSelected(theme)
                        }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = theme == currentTheme,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = theme.displayName)
            }
        }
    }
}
// app/src/main/java/com/example/africanschools/ui/screens/settings/SettingsViewModel.kt

package com.example.africanschools.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.africanschools.data.model.AfricanLanguage
import com.example.africanschools.data.repository.PreferencesRepository
import com.example.africanschools.ui.theme.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _language = MutableStateFlow(preferencesRepository.language)
    val language: StateFlow<AfricanLanguage> = _language.asStateFlow()

    private val _themePreference = MutableStateFlow(preferencesRepository.themePreference)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    fun setLanguage(language: AfricanLanguage) {
        preferencesRepository.language = language
        _language.value = language
    }

    fun setThemePreference(themePreference: ThemePreference) {
        preferencesRepository.themePreference = themePreference
        _themePreference.value = themePreference
    }
}
// app/src/main/java/com/example/africanschools/ui/theme/ThemePreference.kt

package com.example.africanschools.ui.theme

enum class ThemePreference(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}
// app/src/main/java/com/example/africanschools/ui/theme/AfricanSchoolsTheme.kt

package com.example.africanschools.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.africanschools.data.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Composable
fun AfricanSchoolsTheme(
    preferencesRepository: PreferencesRepository,
    content: @Composable () -> Unit
) {
    val themePreference by preferencesRepository.themePreferenceFlow.collectAsState()
    val isDarkTheme = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = if (isDarkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
// app/src/main/java/com/example/africanschools/ui/MainActivity.kt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfricanSchoolsTheme(preferencesRepository = preferencesRepository) {
                val navController = rememberNavController()
                AfricanSchoolsNavGraph(navController = navController)
            }
        }
    }
}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe language preference
        lifecycleScope.launchWhenCreated {
            preferencesRepository.languageFlow.collect { language ->
                updateLocale(language)
            }
        }

        setContent {
            AfricanSchoolsTheme(preferencesRepository = preferencesRepository) {
                val navController = rememberNavController()
                AfricanSchoolsNavGraph(navController = navController)
            }
        }
    }

    private fun updateLocale(language: AfricanLanguage) {
        val locale = Locale(language.localeCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        // You may need to recreate your activity for the changes to take effect
    }
}
// app/src/main/java/com/example/africanschools/data/model/AfricanLanguage.kt

package com.example.africanschools.data.model

enum class AfricanLanguage(val localeCode: String) {
    AMHARIC("am"),      // Ethiopia
    ARABIC("ar"),       // North Africa
    SWAHILI("sw"),      // East Africa
    YORUBA("yo"),       // Nigeria
    IGBO("ig"),         // Nigeria
    HAUSA("ha"),        // Nigeria, Niger
    ZULU("zu"),         // South Africa
    XHOSA("xh"),        // South Africa
    AFRIKAANS("af"),    // South Africa
    SESOTHO("st"),      // South Africa, Lesotho
    SETSWANA("tn"),     // Botswana, South Africa
    ENGLISH("en")       // Default
    // Add more languages as needed
}
class MainRepository @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiService: ApiService,
    private val preferencesRepository: PreferencesRepository
) {
    private val currentLanguageFlow = preferencesRepository.languageFlow

    // Update methods that depend on language
    fun getCountries() = networkBoundResource(
        query = { databaseRepository.getCountriesWithTranslations() },
        fetch = {
            val languageCode = currentLanguageFlow.first().localeCode
            apiService.getCountries(language = languageCode)
        },
        // Rest of the code...
    )
}
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">African Schools</string>
    <string name="settings">Settings</string>
    <string name="language">Language</string>
    <string name="theme">Theme</string>
    <!-- Add more strings as needed -->
</resources>
<!-- res/values-sw/strings.xml -->
<resources>
    <string name="app_name">Shule za Afrika</string>
    <string name="settings">Mipangilio</string>
    <string name="language">Lugha</string>
    <string name="theme">Mandhari</string>
    <!-- Add more translated strings -->
</resources>
object Settings : NavDestination("settings")
composable(NavDestination.Settings.route) {
    SettingsScreen(
        onNavigateUp = navigationActions.navigateUp
    )
}
val navigateToSettings: () -> Unit = {
    navController.navigate(NavDestination.Settings.route)
}
IconButton(onClick = onSettingsClick) {
    Icon(Icons.Default.Settings, contentDescription = "Settings")
} 
