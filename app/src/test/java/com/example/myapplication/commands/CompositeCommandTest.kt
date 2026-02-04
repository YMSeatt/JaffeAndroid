package com.example.myapplication.commands

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CompositeCommandTest {

    @Test
    fun `execute calls all sub-commands in order`() = runTest {
        val cmd1 = mockk<Command>(relaxed = true)
        val cmd2 = mockk<Command>(relaxed = true)
        val composite = CompositeCommand(listOf(cmd1, cmd2), "Test")

        composite.execute()

        coVerify(exactly = 1) { cmd1.execute() }
        coVerify(exactly = 1) { cmd2.execute() }
    }

    @Test
    fun `undo calls all sub-commands in reverse order`() = runTest {
        val cmd1 = mockk<Command>(relaxed = true)
        val cmd2 = mockk<Command>(relaxed = true)
        val composite = CompositeCommand(listOf(cmd1, cmd2), "Test")

        composite.undo()

        coVerify(exactly = 1) { cmd1.undo() }
        coVerify(exactly = 1) { cmd2.undo() }
        // Verify reverse order by checking sequence?
        // MockK can do this with ordered verify
        io.mockk.coVerifyOrder {
            cmd2.undo()
            cmd1.undo()
        }
    }
}
