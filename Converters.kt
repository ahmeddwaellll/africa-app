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
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return gson.fromJson(value, object : TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun fromLongMap(value: Map<String, Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongMap(value: String): Map<String, Long> {
        return gson.fromJson(value, object : TypeToken<Map<String, Long>>() {}.type)
    }
}
