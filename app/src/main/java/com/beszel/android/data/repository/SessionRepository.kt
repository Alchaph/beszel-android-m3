package com.beszel.android.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.beszel.android.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("beszel_session")

class SessionRepository(private val context: Context) {

    private object Keys {
        val SERVER_URL    = stringPreferencesKey("server_url")
        val EMAIL         = stringPreferencesKey("email")
        val USER_ID       = stringPreferencesKey("user_id")
        val TOKEN         = stringPreferencesKey("token")
        val TRUST_CERTS   = booleanPreferencesKey("trust_self_signed")
    }

    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val token = prefs[Keys.TOKEN] ?: return@map null
        val server = prefs[Keys.SERVER_URL] ?: return@map null
        Session(
            serverUrl       = server,
            email           = prefs[Keys.EMAIL] ?: "",
            userId          = prefs[Keys.USER_ID] ?: "",
            token           = token,
            trustSelfSigned = prefs[Keys.TRUST_CERTS] ?: false,
        )
    }

    suspend fun saveSession(session: Session) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.SERVER_URL]  = session.serverUrl
            prefs[Keys.EMAIL]       = session.email
            prefs[Keys.USER_ID]     = session.userId
            prefs[Keys.TOKEN]       = session.token
            prefs[Keys.TRUST_CERTS] = session.trustSelfSigned
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
