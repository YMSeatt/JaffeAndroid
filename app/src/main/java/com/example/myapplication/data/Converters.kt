package com.example.myapplication.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters for handling complex data structures within the SQLite database.
 *
 * This class facilitates the conversion between Kotlin/Java objects (Sets, Maps)
 * and their persisted representations (Strings, JSON). This is particularly useful for:
 * 1. **Fuzzy Student Matching**: Storing sets of variations or tokens.
 * 2. **Flexible Logging**: Storing dynamic quiz and homework mark data as JSON, allowing
 *    the application to support new mark types without requiring a database migration
 *    for every UI-level change.
 */
class Converters {
    /**
     * Converts a comma-separated string back into a [Set] of strings.
     * Used for retrieving multi-value fields like behavior tags or names.
     */
    @TypeConverter
    fun fromString(value: String): Set<String> {
        return value.split(",").toSet()
    }

    /**
     * Serializes a [Set] of strings into a comma-separated string for SQLite storage.
     */
    @TypeConverter
    fun fromSet(set: Set<String>): String {
        return set.joinToString(",")
    }

    companion object {
        /**
         * BOLT: Cache static Gson and TypeToken instances to completely eliminate redundant
         * per-query dynamic class generation and Gson object allocation.
         */
        private val gson = Gson()
        private val mapType = object : TypeToken<Map<String, Int>>() {}.type
    }

    /**
     * Deserializes a JSON string into a [Map] of string keys to integer values.
     * Primarily used for [QuizLog.marksData] to retrieve the count of each mark type.
     */
    @TypeConverter
    fun fromStringMap(value: String): Map<String, Int> {
        return gson.fromJson(value, mapType)
    }

    /**
     * Serializes a [Map] into a JSON string for SQLite storage.
     */
    @TypeConverter
    fun fromMap(map: Map<String, Int>): String {
        return gson.toJson(map)
    }
}
