package com.example.myapplication.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Utility for finding the [Activity] from a given [Context].
 * This safely unwraps [ContextWrapper]s (like those used by Hilt or for theming).
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
