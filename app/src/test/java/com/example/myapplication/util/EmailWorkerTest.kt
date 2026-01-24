package com.example.myapplication.util

import com.example.myapplication.data.exporter.ExportOptions
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class EmailWorkerTest {

    @Test
    fun `test exportOptions serialization works correctly`() {
        // 1. Create a non-default ExportOptions instance.
        val originalOptions = ExportOptions(
            includeBehaviorLogs = false,
            includeSummarySheet = false,
            encrypt = true
        )

        // 2. Serialize it using the correct `Json.encodeToString` method.
        val correctlySerializedOptions = Json.encodeToString(originalOptions)

        // 3. Deserialize it, simulating the worker's behavior.
        val deserializedOptions = Json.decodeFromString<ExportOptions>(correctlySerializedOptions)

        // 4. Assert that the deserialized object is the same as the original.
        // This proves that the serialization-deserialization process is correct.
        assertEquals(
            "Deserialized options should match original",
            originalOptions,
            deserializedOptions
        )
    }
}
