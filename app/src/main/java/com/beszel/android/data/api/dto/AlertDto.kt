package com.beszel.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertConfigDto(
    val id: String = "",
    val user: String = "",
    val system: String = "",
    val name: String = "",
    val value: Float = 0f,
    val min: Int = 1,
    val triggered: Boolean = false,
    val created: String = "",
    val updated: String = "",
    @SerialName("expand") val expand: AlertExpand? = null,
)

@Serializable
data class AlertExpand(
    val system: SystemRecordDto? = null,
)

@Serializable
data class AlertHistoryDto(
    val id: String = "",
    val user: String = "",
    val system: String = "",
    @SerialName("alert_id") val alertId: String = "",
    val name: String = "",
    val value: Float = 0f,
    val created: String = "",
    val resolved: String? = null,
    @SerialName("expand") val expand: AlertExpand? = null,
)

@Serializable
data class UpsertAlertRequest(
    val system: String,
    val name: String,
    val value: Float,
    val min: Int = 1,
)
