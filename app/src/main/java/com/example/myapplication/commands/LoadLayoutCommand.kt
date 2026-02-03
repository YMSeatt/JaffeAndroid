package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to apply a saved layout template to the current seating chart.
 * Reversing this command restores the positions of all students and furniture
 * to their states prior to loading the template.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param layout The [LayoutTemplate] to be applied.
 * @param oldStudents List of student states (including positions) before loading the layout.
 * @param oldFurniture List of furniture states before loading the layout.
 */
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
