package com.example.myapplication.labs.ghost.snapshot

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.FurnitureUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

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
 */
object GhostSnapshotEngine {
    private const val TAG = "GhostSnapshotEngine"
    private const val CANVAS_SIZE = 4000f

    /**
     * Renders the seating chart and saves it to the device's gallery.
     *
     * @param context Android context for MediaStore access.
     * @param students List of students to render.
     * @param furniture List of furniture to render.
     * @param backgroundColor Background color of the canvas.
     * @return The Uri of the saved image, or null if failed.
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

                // Full Name
                val namePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(student.fullName.value, x + w / 2, y + h + 30, namePaint)
            }

            // 5. Save to MediaStore
            val savedUri = saveBitmapToGallery(context, bitmap)
            bitmap.recycle()
            savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture full canvas", e)
            null
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "GhostSnapshot_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GhostSnapshots")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = resolver.openOutputStream(it)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } finally {
                outputStream?.close()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }
}
