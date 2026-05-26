package com.beszel.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class PocketBaseList<T>(
    val page: Int = 1,
    @SerialName("perPage") val perPage: Int = 30,
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    val items: List<T> = emptyList(),
)

@Serializable
data class SystemRecordDto(
    val id: String,
    val name: String,
    val host: String = "",
    val port: String = "45876",
    val status: String = "pending",
    val info: SystemInfoDto = SystemInfoDto(),
    val updated: String = "",
    val created: String = "",
)

@Serializable
data class SystemInfoDto(
    val cpu: Float = 0f,
    val mp: Float = 0f,   // memPct
    val dp: Float = 0f,   // diskPct
    val v: String = "",   // agent version
    val la: List<Float> = emptyList(), // load averages [1m, 5m, 15m]
    val bb: Float = 0f,   // bandwidth bytes
    val t: Int = 0,       // thread count
    val uptime: Long = 0L,
    val b: List<Long> = emptyList(), // [sent_bytes, recv_bytes]
    val bat: List<Float> = emptyList(), // [percent, charge_state]
    val ns: Long = 0L,    // net sent bytes/s
    val nr: Long = 0L,    // net recv bytes/s
    val tp: Float? = null, // temperature peak
)

@Serializable
data class SystemDetailsDto(
    val id: String = "",
    val system: String = "",
    val hostname: String = "",
    val os: Int = 0,
    @SerialName("os_name") val osName: String = "",
    val kernel: String = "",
    val cpu: String = "",
    val arch: String = "",
    val cores: Int = 0,
    val threads: Int = 0,
    val memory: Long = 0L,
    val podman: Boolean = false,
    val updated: String = "",
)

@Serializable
data class SystemStatsDto(
    val id: String = "",
    val system: String = "",
    val type: String = "1m",
    val stats: StatsPayloadDto = StatsPayloadDto(),
    val created: String = "",
)

@Serializable
data class StatsPayloadDto(
    val cpu: Float = 0f,
    val m: Long = 0L,     // mem total MB
    val mu: Long = 0L,    // mem used MB
    val mp: Float = 0f,   // mem pct
    val mb: Long = 0L,    // mem buff/cache
    val s: Long = 0L,     // swap total MB
    val su: Long = 0L,    // swap used MB
    val d: Long = 0L,     // disk total GB
    val du: Long = 0L,    // disk used GB
    val dp: Float = 0f,   // disk pct
    val dr: Long = 0L,    // disk read bytes/s
    val dw: Long = 0L,    // disk write bytes/s
    val ns: Long = 0L,    // net sent bytes/s
    val nr: Long = 0L,    // net recv bytes/s
    val t: Map<String, Float> = emptyMap(), // temperatures
    val la: List<Float> = emptyList(),       // load averages
    val b: List<Long> = emptyList(),         // [sent_bytes, recv_bytes]
    val bat: List<Float> = emptyList(),
)
