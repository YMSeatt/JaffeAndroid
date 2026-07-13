package com.example.myapplication.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Utility for finding the [Activity] from a given [Context].
 *
 * ### Why this is needed:
 * In Jetpack Compose and modern Android development, the provided [Context] is
 * frequently a [ContextWrapper] (e.g., [dagger.hilt.android.internal.managers.ViewComponentManager.FragmentContextWrapper]
 * or a themed context). This can cause [Activity]-specific operations to fail
 * if the context is cast directly.
 *
 * This utility safely unwraps the context hierarchy to access the underlying [Activity],
 * which is required for operations like modifying window flags (e.g., `FLAG_SECURE`)
 * or accessing the `Window` object.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
