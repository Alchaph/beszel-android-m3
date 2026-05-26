package com.beszel.android.data.repository

import com.beszel.android.data.api.ApiResult
import com.beszel.android.data.api.BeszelApiClient
import com.beszel.android.data.api.dto.*
import com.beszel.android.data.model.*
import kotlinx.coroutines.flow.*

class SystemsRepository(private val client: BeszelApiClient) {

    suspend fun getSystems(): Result<List<System>> {
        return when (val r = client.getSystems()) {
            is ApiResult.Success -> Result.success(r.data.items.map { it.toDomain() })
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    suspend fun getSystemDetails(systemId: String): Result<SystemDetails?> {
        return when (val r = client.getSystemDetails(systemId)) {
            is ApiResult.Success -> Result.success(r.data.items.firstOrNull()?.toDomain())
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    suspend fun getSystemStats(
        systemId: String,
        type: String = "1m",
        perPage: Int = 60,
    ): Result<List<SystemStats>> {
        return when (val r = client.getSystemStats(systemId, type, perPage)) {
            is ApiResult.Success -> Result.success(r.data.items.map { it.toDomain() }.reversed())
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    suspend fun getContainers(systemId: String): Result<List<Container>> {
        return when (val r = client.getContainers(systemId)) {
            is ApiResult.Success -> Result.success(r.data.items.map { it.toDomain() })
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    private fun SystemRecordDto.toDomain() = System(
        id             = id,
        name           = name,
        host           = host,
        port           = port,
        status         = status.toSystemStatus(),
        cpu            = info.cpu,
        memPct         = info.mp,
        diskPct        = info.dp,
        netSentBps     = info.ns,
        netRecvBps     = info.nr,
        loadAvg        = info.la,
        uptimeSeconds  = info.uptime,
        agentVersion   = info.v,
        updated        = updated,
    )

    private fun String.toSystemStatus() = when (this) {
        "up"      -> SystemStatus.Up
        "down"    -> SystemStatus.Down
        "paused"  -> SystemStatus.Paused
        "pending" -> SystemStatus.Pending
        else      -> SystemStatus.Unknown
    }

    private fun SystemDetailsDto.toDomain() = SystemDetails(
        systemId          = system,
        hostname          = hostname,
        osName            = osName.ifBlank { listOf("Linux", "macOS", "Windows", "FreeBSD").getOrNull(os) ?: "Unknown" },
        kernel            = kernel,
        cpuModel          = cpu,
        arch              = arch,
        cores             = cores,
        threads           = threads,
        totalMemoryBytes  = memory,
        usingPodman       = podman,
    )

    private fun SystemStatsDto.toDomain() = SystemStats(
        cpu           = stats.cpu,
        memPct        = stats.mp,
        diskPct       = stats.dp,
        memUsedMb     = stats.mu,
        memTotalMb    = stats.m,
        diskUsedGb    = stats.du,
        diskTotalGb   = stats.d,
        netSentBps    = stats.ns,
        netRecvBps    = stats.nr,
        diskReadBps   = stats.dr,
        diskWriteBps  = stats.dw,
        loadAvg       = stats.la,
        temperatures  = stats.t,
        created       = created,
    )

    private fun ContainerRecordDto.toDomain() = Container(
        id           = id,
        name         = name,
        image        = image,
        status       = status,
        health       = health,
        cpu          = cpu,
        memoryBytes  = memory,
    )
}
