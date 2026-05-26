package com.beszel.android.di

import android.content.Context
import com.beszel.android.data.api.BeszelApiClient
import com.beszel.android.data.model.Session
import com.beszel.android.data.repository.AlertsRepository
import com.beszel.android.data.repository.SessionRepository
import com.beszel.android.data.repository.SettingsRepository
import com.beszel.android.data.repository.SystemsRepository

class AppContainer(context: Context) {
    val sessionRepository = SessionRepository(context)
    val settingsRepository = SettingsRepository(context)

    private var _apiClient: BeszelApiClient? = null
    private var _systemsRepository: SystemsRepository? = null
    private var _alertsRepository: AlertsRepository? = null

    fun initClient(session: Session) {
        _apiClient?.close()
        val client = BeszelApiClient(session.serverUrl, session.token, session.trustSelfSigned)
        _apiClient = client
        _systemsRepository = SystemsRepository(client)
        _alertsRepository = AlertsRepository(client)
    }

    fun clearClient() {
        _apiClient?.close()
        _apiClient = null
        _systemsRepository = null
        _alertsRepository = null
    }

    val systemsRepository: SystemsRepository
        get() = _systemsRepository ?: error("Client not initialized — user must be logged in")

    val alertsRepository: AlertsRepository
        get() = _alertsRepository ?: error("Client not initialized — user must be logged in")
}
