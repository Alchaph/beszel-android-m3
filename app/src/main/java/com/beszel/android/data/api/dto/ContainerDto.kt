package com.beszel.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContainerRecordDto(
    val id: String = "",
    val system: String = "",
    val name: String = "",
    val status: String = "",
    val health: Int = 0,
    val cpu: Float = 0f,
    val memory: Long = 0L,
    val net: Long = 0L,
    val image: String = "",
    val ports: String = "",
    val updated: Long = 0L,
)

@Serializable
data class ContainerStatsDto(
    val id: String = "",
    val system: String = "",
    val type: String = "1m",
    val stats: List<ContainerStatEntry> = emptyList(),
    val created: String = "",
)

@Serializable
data class ContainerStatEntry(
    val n: String = "",   // name
    val c: Float = 0f,   // cpu
    val m: Long = 0L,    // mem bytes
    val b: List<Long> = emptyList(), // [sent_bytes, recv_bytes]
)
