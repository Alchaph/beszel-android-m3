package com.beszel.android.data.repository

import com.beszel.android.data.api.ApiResult
import com.beszel.android.data.api.BeszelApiClient
import com.beszel.android.data.api.dto.AlertConfigDto
import com.beszel.android.data.api.dto.AlertHistoryDto
import com.beszel.android.data.model.AlertConfig
import com.beszel.android.data.model.AlertEvent
import com.beszel.android.data.model.AlertSeverity

class AlertsRepository(private val client: BeszelApiClient) {

    suspend fun getAlertHistory(userId: String): Result<List<AlertEvent>> {
        return when (val r = client.getAlertHistory(userId)) {
            is ApiResult.Success -> Result.success(r.data.items.map { it.toDomain() })
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    suspend fun getAlertConfigs(userId: String): Result<List<AlertConfig>> {
        return when (val r = client.getAlertConfigs(userId)) {
            is ApiResult.Success -> Result.success(r.data.items.map { it.toDomain() })
            is ApiResult.Error   -> Result.failure(Exception(r.message))
        }
    }

    private fun AlertHistoryDto.toDomain(): AlertEvent {
        val systemName = expand?.system?.name ?: system
        return AlertEvent(
            id         = id,
            systemId   = system,
            systemName = systemName,
            metricName = name,
            value      = value,
            severity   = name.toSeverity(),
            created    = created,
            resolved   = resolved,
        )
    }

    private fun AlertConfigDto.toDomain(): AlertConfig {
        val systemName = expand?.system?.name ?: system
        return AlertConfig(
            id          = id,
            systemId    = system,
            systemName  = systemName,
            metricName  = name,
            threshold   = value,
            minMinutes  = min,
            triggered   = triggered,
        )
    }

    private fun String.toSeverity(): AlertSeverity = when (this.lowercase()) {
        "status" -> AlertSeverity.Error
        "cpu", "loadavg1", "loadavg5", "loadavg15", "temperature", "gpu" -> AlertSeverity.Warning
        "memory", "disk", "bandwidth", "battery" -> AlertSeverity.Warning
        else -> AlertSeverity.Info
    }
}
