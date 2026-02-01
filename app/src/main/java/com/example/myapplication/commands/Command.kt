package com.example.myapplication.commands

interface Command {
    suspend fun execute()
    suspend fun undo()
    fun getDescription(): String
}
