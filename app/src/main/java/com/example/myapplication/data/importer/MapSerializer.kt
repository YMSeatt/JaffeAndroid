package com.example.myapplication.data.importer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
