package com.example.myapplication.util

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

/**
 * captureComposable: A high-fidelity visual serialization utility.
 *
 * This function handles the complex task of capturing a [Bitmap] representation of a
 * [View] (typically a `ComposeView`) that may contain hardware-accelerated content,
 * such as experimental AGSL shaders or high-performance animations.
 *
 * ### The "Capture Paradox":
 * In modern Android development, especially with Jetpack Compose, the standard
 * `View.draw(Canvas)` approach often results in blank or incomplete images because
 * it doesn't always capture layers rendered directly by the GPU.
 *
 * ### Implementation Strategy:
 * 1. **Modern Devices (API 26+)**: Uses the [PixelCopy] API. This service performs
 *    a hardware-level copy of the window's surface buffer. This is the only reliable
 *    way to capture AGSL-heavy content from the "Ghost Lab" experiments.
 * 2. **Coordinate Alignment**: Employs [View.getLocationInWindow] to precisely
 *    calculate the capture [Rect], ensuring the serialized image matches the
 *    visual bounds observed by the user.
 * 3. **Legacy Fallback**: On older devices, it reverts to the [Canvas] drawing
 *    model. While less accurate for complex shaders, it provides baseline compatibility.
 *
 * ### Lifecycle & Performance:
 * This is a `suspend` function that utilizes [suspendCancellableCoroutine] to bridge
 * the asynchronous [PixelCopy] callback. It must be called from a coroutine, and
 * internally manages its execution on the Main Looper as required by the system API.
 *
 * @param view The specific View or ComposeView hierarchy to capture.
 * @param window The active system [Window] providing the surface buffer.
 * @return A [Bitmap] snapshot, or null if the hardware copy request fails.
 */
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
