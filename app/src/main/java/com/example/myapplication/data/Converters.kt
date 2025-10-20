package com.example.myapplication.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String): Set<String> {
        return value.split(",").toSet()
    }

    @TypeConverter
    fun fromSet(set: Set<String>): String {
        return set.joinToString(",")
    }
}
