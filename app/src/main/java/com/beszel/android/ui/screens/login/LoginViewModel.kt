package com.beszel.android.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.beszel.android.data.api.ApiResult
import com.beszel.android.data.api.BeszelApiClient
import com.beszel.android.data.model.Session
import com.beszel.android.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val step: LoginStep = LoginStep.Server,
    val serverUrl: String = "https://",
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val trustSelfSigned: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val authProviders: List<String> = emptyList(),
)

enum class LoginStep { Server, Credentials }

class LoginViewModel(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun setServerUrl(url: String) = _uiState.update { it.copy(serverUrl = url, error = null) }
    fun setEmail(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun setPassword(v: String) = _uiState.update { it.copy(password = v, error = null) }
    fun toggleShowPassword() = _uiState.update { it.copy(showPassword = !it.showPassword) }
    fun setTrustSelfSigned(v: Boolean) = _uiState.update { it.copy(trustSelfSigned = v) }
    fun goBackToServer() = _uiState.update { it.copy(step = LoginStep.Server, error = null) }

    fun connectToServer(onSessionReady: (Session) -> Unit = {}) {
        val state = _uiState.value
        val rawUrl = state.serverUrl.trim().trimEnd('/')
        if (!rawUrl.matches(Regex("^https?://.+"))) {
            _uiState.update { it.copy(error = "Enter a valid URL (https://hub.example.com)") }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val client = BeszelApiClient(rawUrl, "", state.trustSelfSigned)
            when (val info = client.getHubInfo()) {
                is ApiResult.Success -> {
                    val authMethods = client.getAuthMethods()
                    val providers = (authMethods as? ApiResult.Success)
                        ?.data?.authProviders?.map { it.name } ?: emptyList()
                    _uiState.update {
                        it.copy(
                            loading = false,
                            serverUrl = rawUrl,
                            step = LoginStep.Credentials,
                            authProviders = providers,
                        )
                    }
                }
                is ApiResult.Error -> {
                    val msg = when {
                        info.message.contains("timeout", true) ||
                        info.message.contains("connect", true) ->
                            "Cannot reach $rawUrl. Check the URL and network."
                        info.code == 404 -> "No Beszel hub found at that URL."
                        else -> info.message
                    }
                    _uiState.update { it.copy(loading = false, error = msg) }
                }
            }
            client.close()
        }
    }

    fun signIn(onSuccess: (Session) -> Unit) {
        val state = _uiState.value
        if (!state.email.contains('@')) {
            _uiState.update { it.copy(error = "Enter a valid email address") }
            return
        }
        if (state.password.length < 4) {
            _uiState.update { it.copy(error = "Password is too short") }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val client = BeszelApiClient(state.serverUrl, "", state.trustSelfSigned)
            when (val result = client.authWithPassword(state.email, state.password)) {
                is ApiResult.Success -> {
                    val session = Session(
                        serverUrl       = state.serverUrl,
                        email           = result.data.record.email.ifBlank { state.email },
                        userId          = result.data.record.id,
                        token           = result.data.token,
                        trustSelfSigned = state.trustSelfSigned,
                    )
                    sessionRepository.saveSession(session)
                    _uiState.update { it.copy(loading = false) }
                    onSuccess(session)
                }
                is ApiResult.Error -> {
                    val msg = when {
                        result.code == 400 -> "Wrong email or password"
                        result.code == 403 -> "Account disabled or forbidden"
                        else -> result.message
                    }
                    _uiState.update { it.copy(loading = false, error = msg) }
                }
            }
            client.close()
        }
    }

    class Factory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(sessionRepository) as T
        }
    }
}
