package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun captureComposable(
    view: View,
    window: Window
): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return suspendCancellableCoroutine { continuation ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            val x = locationOfViewInWindow[0]
            val y = locationOfViewInWindow[1]
            val scope = Rect(x, y, x + view.width, y + view.height)

            PixelCopy.request(
                window,
                scope,
                bitmap,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        continuation.resume(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    } else {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
