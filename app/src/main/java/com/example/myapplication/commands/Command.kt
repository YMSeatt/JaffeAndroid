package com.example.myapplication.commands

interface Command {
    fun execute()
    fun undo()
}
