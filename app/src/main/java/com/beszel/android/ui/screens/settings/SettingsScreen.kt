package com.beszel.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.Session
import com.beszel.android.ui.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    session: Session,
    onSignOut: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Column {
                Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Spacer(Modifier.weight(1f))
                }
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            // Profile card
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                session.email.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineSmall.copy(color = colors.onPrimary),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                session.email,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSecondaryContainer,
                                maxLines = 1,
                            )
                            Text(
                                session.serverUrl,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = colors.onSecondaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Appearance section
            item { SectionLabel("Appearance") }
            item {
                SettingRow(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    sub = if (settings.darkMode) "Dark" else "Light",
                    trailing = {
                        Switch(checked = settings.darkMode, onCheckedChange = viewModel::setDarkMode)
                    }
                )
            }
            item {
                // Accent color picker
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Palette, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("Accent color", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(12.dp))
                    val accentOptions = listOf(
                        "teal"   to Color(0xFF006A60),
                        "blue"   to Color(0xFF0061A4),
                        "purple" to Color(0xFF6B4EA0),
                        "rose"   to Color(0xFFB3261E),
                        "amber"  to Color(0xFF7A5800),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 56.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        accentOptions.forEach { (key, color) ->
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.setAccentColor(key) }
                                    .then(
                                        if (settings.accentColor == key)
                                            Modifier.border(3.dp, colors.primary, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (settings.accentColor == key) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            item {
                SettingRow(
                    icon = Icons.Default.ViewList,
                    title = "List density",
                    sub = if (settings.density == "compact") "Compact" else "Comfortable",
                    trailing = {
                        Switch(
                            checked = settings.density == "compact",
                            onCheckedChange = { viewModel.setDensity(if (it) "compact" else "comfortable") },
                        )
                    }
                )
            }

            // Notifications section
            item { SectionLabel("Notifications") }
            item {
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = "Push notifications",
                    sub = "Critical & warning alerts",
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                )
            }

            // Hub section
            item { SectionLabel("Hub") }
            item {
                SettingRow(
                    icon = Icons.Default.Storage,
                    title = "Hub address",
                    sub = session.serverUrl,
                    mono = true,
                )
            }
            item {
                SettingRow(
                    icon = Icons.Default.Info,
                    title = "Agent version",
                    sub = "beszel-agent connected",
                    mono = false,
                )
            }

            // About section
            item { SectionLabel("About") }
            item {
                SettingRow(
                    icon = Icons.Default.Info,
                    title = "App version",
                    sub = "1.0.0",
                )
            }

            // Sign out
            item {
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign out")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = androidx.compose.ui.unit.TextUnit(0.5f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    sub: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    mono: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null)
        Modifier.fillMaxWidth().clickable(onClick = onClick)
    else Modifier.fillMaxWidth()

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 14.dp).heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (sub != null) {
                Text(
                    sub,
                    style = MaterialTheme.typography.bodyMedium.let {
                        if (mono) it.copy(fontFamily = FontFamily.Monospace) else it
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        trailing?.invoke()
    }
}
