package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_emails")
data class PendingEmail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientAddress: String,
    val subject: String,
    val body: String,
    val timestamp: Long
)
