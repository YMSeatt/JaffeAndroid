package com.example.myapplication.util

import android.util.Log
import java.io.File

/**
 * FileLocker: A utility for managing file-level access permissions.
 *
 * This class provides logic parity with `Python/data_locker.py`, allowing the application
 * to toggle files between read-only and writable states. This acts as a deterrent
 * against accidental modification of critical data files.
 */
object FileLocker {
    private const val TAG = "FileLocker"

    /**
     * Locks the specified file by making it read-only.
     * Matches the behavior of `lock_file` in Python.
     *
     * @param file The file to lock.
     * @return True if the operation was successful, false otherwise.
     */
    fun lock(file: File): Boolean {
        Log.d(TAG, "Attempting to lock '${file.absolutePath}'...")
        if (!file.exists()) {
            Log.w(TAG, "Warning: File '${file.absolutePath}' not found. Cannot lock.")
            return false
        }

        return try {
            // Set readable for all, consistent with Python's 444 (r--r--r--)
            file.setReadable(true, false)
            val success = file.setReadOnly()
            if (success) {
                Log.i(TAG, "'${file.name}' has been locked (set to read-only).")
            } else {
                Log.e(TAG, "Failed to set '${file.name}' to read-only.")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while locking the file: ${e.message}")
            false
        }
    }

    /**
     * Unlocks the specified file by making it writable for the owner.
     * Matches the behavior of `unlock_file` in Python.
     *
     * @param file The file to unlock.
     * @return True if the operation was successful, false otherwise.
     */
    fun unlock(file: File): Boolean {
        Log.d(TAG, "Attempting to unlock '${file.absolutePath}'...")
        if (!file.exists()) {
            Log.w(TAG, "Warning: File '${file.absolutePath}' not found. Cannot unlock.")
            return false
        }

        return try {
            // Set readable for all, and writable for owner, consistent with Python's 644 (rw-r--r--)
            file.setReadable(true, false)
            val success = file.setWritable(true, true)
            if (success) {
                Log.i(TAG, "'${file.name}' has been unlocked (set to writable).")
            } else {
                Log.e(TAG, "Failed to set '${file.name}' to writable.")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while unlocking the file: ${e.message}")
            false
        }
    }
}
