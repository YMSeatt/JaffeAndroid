package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

class LoadLayoutCommand(
    private val viewModel: SeatingChartViewModel,
    private val layout: LayoutTemplate,
    private val oldStudents: List<Student>,
    private val oldFurniture: List<Furniture>
) : Command {
    override suspend fun execute() {
        viewModel.internalLoadLayout(layout)
    }

    override suspend fun undo() {
        viewModel.internalUpdateAll(oldStudents, oldFurniture)
    }

    override fun getDescription(): String = "Load layout: ${layout.name}"
}
