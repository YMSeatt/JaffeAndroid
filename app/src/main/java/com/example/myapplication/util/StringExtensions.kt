package com.example.myapplication.util

import java.util.Locale

fun String.toTitleCase(): String {
    return this.lowercase(Locale.getDefault()).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
