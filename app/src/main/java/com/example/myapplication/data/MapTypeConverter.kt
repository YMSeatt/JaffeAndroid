package com.example.myapplication.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapTypeConverter {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Int>>() {}.type

    @TypeConverter
    fun fromString(value: String): Map<String, Int> {
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Int>): String {
        return gson.toJson(map)
    }
}
