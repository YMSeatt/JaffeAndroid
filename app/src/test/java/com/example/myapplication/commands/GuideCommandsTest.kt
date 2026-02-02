package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideType
import com.example.myapplication.viewmodel.SeatingChartViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GuideCommandsTest {

    private val viewModel = mockk<SeatingChartViewModel>(relaxed = true)

    @Test
    fun `AddGuideCommand execute calls internalAddGuide`() = runBlocking {
        val guide = Guide(type = GuideType.VERTICAL, position = 100f)
        coEvery { viewModel.internalAddGuide(any()) } returns 1L

        val command = AddGuideCommand(viewModel, guide)
        command.execute()

        coVerify { viewModel.internalAddGuide(guide) }
    }

    @Test
    fun `AddGuideCommand undo calls internalDeleteGuide with correct id`() = runBlocking {
        val guide = Guide(type = GuideType.VERTICAL, position = 100f)
        coEvery { viewModel.internalAddGuide(any()) } returns 1L

        val command = AddGuideCommand(viewModel, guide)
        command.execute()
        command.undo()

        coVerify { viewModel.internalDeleteGuide(guide.copy(id = 1L)) }
    }

    @Test
    fun `DeleteGuideCommand execute calls internalDeleteGuide`() = runBlocking {
        val guide = Guide(id = 1L, type = GuideType.HORIZONTAL, position = 50f)

        val command = DeleteGuideCommand(viewModel, guide)
        command.execute()

        coVerify { viewModel.internalDeleteGuide(guide) }
    }

    @Test
    fun `DeleteGuideCommand undo calls internalAddGuide`() = runBlocking {
        val guide = Guide(id = 1L, type = GuideType.HORIZONTAL, position = 50f)

        val command = DeleteGuideCommand(viewModel, guide)
        command.undo()

        coVerify { viewModel.internalAddGuide(guide) }
    }

    @Test
    fun `MoveGuideCommand execute updates to new position`() = runBlocking {
        val guide = Guide(id = 1L, type = GuideType.VERTICAL, position = 100f)

        val command = MoveGuideCommand(viewModel, guide, 100f, 200f)
        command.execute()

        coVerify { viewModel.internalUpdateGuide(guide.copy(position = 200f)) }
    }

    @Test
    fun `MoveGuideCommand undo reverts to old position`() = runBlocking {
        val guide = Guide(id = 1L, type = GuideType.VERTICAL, position = 100f)

        val command = MoveGuideCommand(viewModel, guide, 100f, 200f)
        command.undo()

        coVerify { viewModel.internalUpdateGuide(guide.copy(position = 100f)) }
    }
}
