package com.example.myapplication.data.importer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * MapSerializer: A specialized KSerializer for [Map<String, Double>].
 *
 * This utility facilitates the serialization and deserialization of homework mark data
 * when ingesting legacy fragmented JSON exports. It ensures that the `marks_data`
 * maps in [HomeworkLogEntry] are correctly processed using Kotlin Serialization's
 * built-in [MapSerializer].
 */
object MapSerializer : KSerializer<Map<String, Double>> {
    private val mapSerializer = MapSerializer(String.serializer(), Double.serializer())

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, Double>) {
        mapSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, Double> {
        return mapSerializer.deserialize(decoder)
    }
}
