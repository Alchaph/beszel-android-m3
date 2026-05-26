package com.beszel.android.data.model

enum class SystemStatus { Up, Down, Paused, Pending, Unknown }

data class System(
    val id: String,
    val name: String,
    val host: String,
    val port: String,
    val status: SystemStatus,
    val cpu: Float,
    val memPct: Float,
    val diskPct: Float,
    val netSentBps: Long,
    val netRecvBps: Long,
    val loadAvg: List<Float>,
    val uptimeSeconds: Long,
    val agentVersion: String,
    val updated: String,
)

data class SystemDetails(
    val systemId: String,
    val hostname: String,
    val osName: String,
    val kernel: String,
    val cpuModel: String,
    val arch: String,
    val cores: Int,
    val threads: Int,
    val totalMemoryBytes: Long,
    val usingPodman: Boolean,
)

data class SystemStats(
    val cpu: Float,
    val memPct: Float,
    val diskPct: Float,
    val memUsedMb: Long,
    val memTotalMb: Long,
    val diskUsedGb: Long,
    val diskTotalGb: Long,
    val netSentBps: Long,
    val netRecvBps: Long,
    val diskReadBps: Long,
    val diskWriteBps: Long,
    val loadAvg: List<Float>,
    val temperatures: Map<String, Float>,
    val created: String,
)

data class Container(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val health: Int,
    val cpu: Float,
    val memoryBytes: Long,
)

enum class AlertSeverity { Error, Warning, Info }

data class AlertEvent(
    val id: String,
    val systemId: String,
    val systemName: String,
    val metricName: String,
    val value: Float,
    val severity: AlertSeverity,
    val created: String,
    val resolved: String?,
) {
    val isActive: Boolean get() = resolved == null || resolved.isBlank()
}

data class AlertConfig(
    val id: String,
    val systemId: String,
    val systemName: String,
    val metricName: String,
    val threshold: Float,
    val minMinutes: Int,
    val triggered: Boolean,
)

data class Session(
    val serverUrl: String,
    val email: String,
    val userId: String,
    val token: String,
    val trustSelfSigned: Boolean = false,
)

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1   -> "%.1f GB".format(gb)
        mb >= 1   -> "%.1f MB".format(mb)
        kb >= 1   -> "%.0f KB".format(kb)
        else      -> "$bytes B"
    }
}

fun formatBytesPerSec(bps: Long): String {
    if (bps <= 0) return "0 B/s"
    val kb = bps / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1   -> "%.1f GB/s".format(gb)
        mb >= 1   -> "%.1f MB/s".format(mb)
        kb >= 1   -> "%.0f KB/s".format(kb)
        else      -> "$bps B/s"
    }
}

fun formatUptime(seconds: Long): String {
    if (seconds <= 0) return "—"
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        days > 0  -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else      -> "${minutes}m"
    }
}
