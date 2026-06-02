package com.example.myapplication.labs.ghost.snapshot

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.util.maskStudentName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * GhostSnapshotEngine: A high-fidelity spatial archival engine.
 *
 * Unlike standard screenshots that only capture the visible viewport, Ghost Snapshot
 * renders the entire 4000x4000 logical canvas into a high-resolution Bitmap.
 *
 * ### Architectural Intent:
 * This engine provides a "God View" of the classroom, preserving the spatial
 * relationships of all students and furniture regardless of the user's current
 * zoom level or screen size.
 *
 * ### BOLT ⚡ Optimization:
 * - Uses [Dispatchers.IO] for heavy Bitmap rendering and disk I/O.
 * - Implements manual rendering using [android.graphics.Canvas] to bypass
 *   the overhead of the Compose UI tree for a 16-megapixel image.
 *
 * ### Shield (Security Hardening):
 * 1. **PII Masking**: Student names are automatically masked (e.g., "J. DOE") during
 *    the rendering process to prevent data leakage in shared images.
 * 2. **Secure Storage**: Snapshots are stored in the app's internal shared cache
 *    directory instead of the public Gallery.
 * 3. **Controlled Access**: Uses [FileProvider] to grant temporary access to the
 *    snapshot URI for sharing, ensuring no other apps can access the PII without
 *    explicit user intent.
 */
object GhostSnapshotEngine {
    private const val TAG = "GhostSnapshotEngine"
    private const val CANVAS_SIZE = 4000f

    /**
     * Renders the seating chart and saves it to the app's internal secure cache.
     *
     * @param context Android context for file operations and FileProvider access.
     * @param students List of students to render.
     * @param furniture List of furniture to render.
     * @param backgroundColor Background color of the canvas.
     * @return The secure content Uri of the saved image, or null if failed.
     */
    suspend fun captureFullCanvas(
        context: Context,
        students: List<StudentUiItem>,
        furniture: List<FurnitureUiItem>,
        backgroundColor: Int = Color.BLACK
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = Bitmap.createBitmap(CANVAS_SIZE.toInt(), CANVAS_SIZE.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Draw Background
            canvas.drawColor(backgroundColor)

            // 2. Draw Grid (Logic Parity with Seating Chart)
            val gridPaint = Paint().apply {
                color = Color.parseColor("#1A00FFFF") // Low opacity cyan
                strokeWidth = 2f
            }
            for (i in 0..CANVAS_SIZE.toInt() step 100) {
                canvas.drawLine(i.toFloat(), 0f, i.toFloat(), CANVAS_SIZE, gridPaint)
                canvas.drawLine(0f, i.toFloat(), CANVAS_SIZE, i.toFloat(), gridPaint)
            }

            // 3. Draw Furniture
            val furniturePaint = Paint().apply {
                color = Color.parseColor("#4D00FFFF") // Medium opacity cyan
                style = Paint.Style.STROKE
                strokeWidth = 4f
                pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
            }
            val textPaint = Paint().apply {
                color = Color.CYAN
                textSize = 30f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
            }

            furniture.forEach { item ->
                val x = item.xPosition.value
                val y = item.yPosition.value
                val w = item.width.value
                val h = item.height.value
                canvas.drawRect(x, y, x + w, y + h, furniturePaint)
                canvas.drawText(item.name.value, x + w / 2, y + h + 40, textPaint)
            }

            // 4. Draw Students
            students.forEach { student ->
                val x = student.xPosition.value
                val y = student.yPosition.value
                val w = student.width.value
                val h = student.height.value

                // Box
                val boxPaint = Paint().apply {
                    color = Color.parseColor(student.backgroundColor.value)
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.parseColor(student.outlineColor.value)
                    style = Paint.Style.STROKE
                    strokeWidth = student.outlineThickness.value.toFloat()
                }

                val rect = RectF(x, y, x + w, y + h)
                val radius = student.cornerRadius.value.toFloat()
                canvas.drawRoundRect(rect, radius, radius, boxPaint)
                canvas.drawRoundRect(rect, radius, radius, borderPaint)

                // Text (Initials)
                val initialsPaint = Paint().apply {
                    color = Color.parseColor(student.textColor.value)
                    textSize = w * 0.4f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(student.initials.value, x + w / 2, y + h / 2 + (initialsPaint.textSize / 3), initialsPaint)

                // Full Name (HARDEN: Masked PII)
                val namePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(maskStudentName(student.fullName.value), x + w / 2, y + h + 30, namePaint)
            }

            // 5. Save to Internal Cache
            val savedUri = saveBitmapToCache(context, bitmap)
            bitmap.recycle()
            savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture full canvas", e)
            null
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        val filename = "GhostSnapshot_${System.currentTimeMillis()}.png"
        val sharedDir = File(context.cacheDir, "shared")
        if (!sharedDir.exists()) sharedDir.mkdirs()

        val file = File(sharedDir, filename)

        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            return FileProvider.getUriForFile(
                context,
                "com.example.myapplication.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save snapshot to cache", e)
            return null
        }
    }
}
