package com.example.myapplication.data.importer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * AnySerializer: A specialized KSerializer for the [Any] type.
 *
 * This serializer is used within the [Classroom] data model to handle polymorphic values
 * in the undo and redo stacks during legacy data ingestion. It serializes objects
 * by calling their `toString()` method and deserializes them back as [String]s.
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }
}
