package com.example.myapplication.data.exporter

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ExporterSanitizationTest {

    private val context: Context = mockk()
    private val exporter = Exporter(context)

    @Test
    fun `sanitize should prepend quote to strings starting with triggers`() {
        assertEquals("'=SUM(1,2)", exporter.sanitize("=SUM(1,2)"))
        assertEquals("'+Score", exporter.sanitize("+Score"))
        assertEquals("'-Negative", exporter.sanitize("-Negative"))
        assertEquals("'@Admin", exporter.sanitize("@Admin"))
        assertEquals("'%Percent", exporter.sanitize("%Percent"))
    }

    @Test
    fun `sanitize should prepend quote to strings with leading whitespace followed by triggers`() {
        assertEquals("' =SUM(1,2)", exporter.sanitize(" =SUM(1,2)"))
        assertEquals("'  +Score", exporter.sanitize("  +Score"))
        assertEquals("'\t-Negative", exporter.sanitize("\t-Negative"))
        assertEquals("'\n@Admin", exporter.sanitize("\n@Admin"))
        assertEquals("' %Percent", exporter.sanitize(" %Percent"))
    }

    @Test
    fun `sanitize should not prepend quote to normal strings`() {
        assertEquals("John Doe", exporter.sanitize("John Doe"))
        assertEquals("123", exporter.sanitize("123"))
        assertEquals("Note: score is 100", exporter.sanitize("Note: score is 100"))
        assertEquals("", exporter.sanitize(""))
        assertEquals("", exporter.sanitize(null))
    }

    @Test
    fun `sanitize should handle edge cases with only whitespace`() {
        assertEquals("", exporter.sanitize("   "))
        assertEquals("", exporter.sanitize("\t\n"))
    }
}
