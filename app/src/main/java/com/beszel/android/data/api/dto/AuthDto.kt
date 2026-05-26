package com.beszel.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val record: UserRecord,
)

@Serializable
data class UserRecord(
    val id: String,
    val email: String,
    val username: String = "",
    val role: String = "user",
)

@Serializable
data class AuthMethodsResponse(
    @SerialName("authProviders") val authProviders: List<AuthProvider> = emptyList(),
    @SerialName("usernamePassword") val usernamePassword: Boolean = true,
    @SerialName("emailPassword") val emailPassword: Boolean = true,
)

@Serializable
data class AuthProvider(
    val name: String,
    @SerialName("displayName") val displayName: String = "",
    @SerialName("authUrl") val authUrl: String = "",
    val state: String = "",
    @SerialName("codeVerifier") val codeVerifier: String = "",
    @SerialName("codeChallenge") val codeChallenge: String = "",
    @SerialName("codeChallengeMethod") val codeChallengeMethod: String = "",
)

