package com.beszel.android.data.api

import com.beszel.android.data.api.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.net.ssl.HostnameVerifier
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
}

// Carries the already-read error body so safeCall doesn't re-read a consumed channel.
private class ApiErrorException(val code: Int, val body: String) : Exception("HTTP $code")

class BeszelApiClient(
    private val baseUrl: String,
    private val token: String,
    trustSelfSigned: Boolean = false,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val httpClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        // Read error body here, before the channel is consumed by anything else.
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val body = try { response.bodyAsText() } catch (_: Exception) { "" }
                    throw ApiErrorException(response.status.value, body)
                }
            }
        }
        if (trustSelfSigned) {
            engine {
                sslManager = { httpsURLConnection ->
                    val trustAll = object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                    val sslContext = SSLContext.getInstance("TLS").apply {
                        init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
                    }
                    httpsURLConnection.sslSocketFactory = sslContext.socketFactory
                    httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
            }
        }
        defaultRequest {
            url(baseUrl)
            if (token.isNotBlank()) header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
        }
    }

    private val normalizedBase = baseUrl.trimEnd('/')

    suspend fun authWithPassword(email: String, password: String): ApiResult<AuthResponse> = safeCall {
        httpClient.post("$normalizedBase/api/collections/users/auth-with-password") {
            setBody(mapOf("identity" to email, "password" to password))
        }.body()
    }

    suspend fun getAuthMethods(): ApiResult<AuthMethodsResponse> = safeCall {
        httpClient.get("$normalizedBase/api/collections/users/list-auth-methods").body()
    }

    suspend fun getSystems(): ApiResult<PocketBaseList<SystemRecordDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/systems/records") {
            parameter("perPage", 200)
            parameter("sort", "name")
        }.body()
    }

    suspend fun getSystemDetails(systemId: String): ApiResult<PocketBaseList<SystemDetailsDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/system_details/records") {
            parameter("filter", "system=\"$systemId\"")
            parameter("perPage", 1)
        }.body()
    }

    suspend fun getSystemStats(
        systemId: String,
        type: String = "1m",
        perPage: Int = 60,
    ): ApiResult<PocketBaseList<SystemStatsDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/system_stats/records") {
            parameter("filter", "system=\"$systemId\"&&type=\"$type\"")
            parameter("sort", "-created")
            parameter("perPage", perPage)
        }.body()
    }

    suspend fun getContainers(systemId: String): ApiResult<PocketBaseList<ContainerRecordDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/containers/records") {
            parameter("filter", "system=\"$systemId\"")
            parameter("perPage", 100)
            parameter("sort", "name")
        }.body()
    }

    suspend fun getAlertHistory(userId: String): ApiResult<PocketBaseList<AlertHistoryDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/alerts_history/records") {
            parameter("filter", "user=\"$userId\"")
            parameter("sort", "-created")
            parameter("perPage", 100)
            parameter("expand", "system")
        }.body()
    }

    suspend fun getAlertConfigs(userId: String): ApiResult<PocketBaseList<AlertConfigDto>> = safeCall {
        httpClient.get("$normalizedBase/api/collections/alerts/records") {
            parameter("filter", "user=\"$userId\"")
            parameter("perPage", 200)
            parameter("expand", "system")
        }.body()
    }

    fun subscribeSse(topics: List<String>): Flow<SseEvent> = flow {
        val topicsParam = topics.joinToString(",")
        val response: HttpResponse = httpClient.get("$normalizedBase/api/realtime") {
            parameter("subscribe", topicsParam)
            header(HttpHeaders.Accept, "text/event-stream")
            timeout { socketTimeoutMillis = Long.MAX_VALUE }
        }
        val reader = response.bodyAsChannel().toInputStream().bufferedReader(Charsets.UTF_8)
        var eventType = ""
        var eventData = ""
        while (true) {
            val line = withContext(Dispatchers.IO) { reader.readLine() } ?: break
            when {
                line.startsWith("event:") -> eventType = line.removePrefix("event:").trim()
                line.startsWith("data:")  -> eventData = line.removePrefix("data:").trim()
                line.isEmpty() && eventData.isNotEmpty() -> {
                    emit(SseEvent(eventType, eventData))
                    eventType = ""
                    eventData = ""
                }
            }
        }
    }

    fun close() = httpClient.close()

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (e: ApiErrorException) {
        ApiResult.Error(parseErrorMessage(e.body), e.code)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Unknown error")
    }

    private fun parseErrorMessage(body: String): String {
        return try {
            val msg = json.parseToJsonElement(body)
                .let { it as? kotlinx.serialization.json.JsonObject }
                ?.get("message")?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
            msg ?: body
        } catch (_: Exception) { body }
    }
}

data class SseEvent(val type: String, val data: String)

class UnauthenticatedException : Exception("Session expired")
