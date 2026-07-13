package com.example.myapplication.labs.ghost.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.example.myapplication.MainActivity
import com.example.myapplication.util.maskStudentName
import com.example.myapplication.R

/**
 * GhostSeedEngine: Manages "Neural Seeds" (Native Android Shortcuts).
 *
 * This engine facilitates the creation of pinned and dynamic shortcuts on the Android
 * home screen. It allows teachers to "seed" specific students to their home screen for
 * instant access to their Neural Dossier.
 */
object GhostSeedEngine {

    const val ACTION_OPEN_DOSSIER = "com.example.myapplication.labs.ghost.ACTION_OPEN_DOSSIER"
    const val EXTRA_STUDENT_ID = "EXTRA_STUDENT_ID"

    /**
     * Requests the OS to pin a "Neural Seed" (shortcut) for a specific student.
     * Only available on API 26+.
     */
    fun pinStudentSeed(context: Context, studentId: Long, studentName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

            if (shortcutManager.isRequestPinShortcutSupported) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_OPEN_DOSSIER
                    putExtra(EXTRA_STUDENT_ID, studentId)
                    // Ensure the intent opens a fresh dossier context
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val maskedName = maskStudentName(studentName)
                val shortcut = ShortcutInfo.Builder(context, "student_$studentId")
                    .setShortLabel(maskedName)
                    .setLongLabel("Neural Seed: $maskedName")
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .build()

                shortcutManager.requestPinShortcut(shortcut, null)
            }
        }
    }

    /**
     * Refreshes the "Recent Seeds" dynamic shortcuts based on recent activity.
     */
    fun refreshDynamicSeeds(context: Context, recentStudents: List<Pair<Long, String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

            val dynamicShortcuts = recentStudents.take(4).map { (id, name) ->
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_OPEN_DOSSIER
                    putExtra(EXTRA_STUDENT_ID, id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val maskedName = maskStudentName(name)
                ShortcutInfo.Builder(context, "dynamic_student_$id")
                    .setShortLabel(maskedName)
                    .setLongLabel("Recent: $maskedName")
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .build()
            }

            shortcutManager.dynamicShortcuts = dynamicShortcuts
        }
    }
}
