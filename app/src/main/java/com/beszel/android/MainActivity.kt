package com.beszel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.Session
import com.beszel.android.navigation.AppNavGraph
import com.beszel.android.ui.theme.AccentColor
import com.beszel.android.ui.theme.BeszelTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BeszelApp
        val container = app.container

        setContent {
            var initialSession by remember { mutableStateOf<Session?>(null) }
            var sessionLoaded by remember { mutableStateOf(false) }

            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = com.beszel.android.data.repository.AppSettings()
            )

            LaunchedEffect(Unit) {
                container.sessionRepository.session.first().let { session ->
                    initialSession = session
                    if (session != null) container.initClient(session)
                    sessionLoaded = true
                }
            }

            if (sessionLoaded) {
                val accent = when (settings.accentColor) {
                    "blue"   -> AccentColor.Blue
                    "purple" -> AccentColor.Purple
                    "rose"   -> AccentColor.Rose
                    "amber"  -> AccentColor.Amber
                    else     -> AccentColor.Teal
                }
                BeszelTheme(dark = settings.darkMode, accent = accent) {
                    AppNavGraph(
                        container = container,
                        initialSession = initialSession,
                    )
                }
            }
        }
    }
}
