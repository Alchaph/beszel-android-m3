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

// Matches the /api/collections/users/auth-methods response shape in Beszel 0.8+
@Serializable
data class AuthMethodsResponse(
    val password: PasswordAuthConfig = PasswordAuthConfig(),
    val oauth2: OAuth2Config = OAuth2Config(),
)

@Serializable
data class PasswordAuthConfig(
    val enabled: Boolean = true,
)

@Serializable
data class OAuth2Config(
    val enabled: Boolean = false,
    val providers: List<AuthProvider> = emptyList(),
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
