package com.beszel.android.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.AlertEvent
import com.beszel.android.data.model.AlertSeverity
import com.beszel.android.ui.theme.onWarningContainer
import com.beszel.android.ui.theme.warningContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = viewModel::load) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)) {
                    Text("Alerts", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${state.activeCount} active · ${state.resolvedCount} resolved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !state.showResolved,
                        onClick = { viewModel.setShowResolved(false) },
                        label = { Text("Active ${state.activeCount}") },
                        leadingIcon = if (!state.showResolved) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null,
                    )
                    FilterChip(
                        selected = state.showResolved,
                        onClick = { viewModel.setShowResolved(true) },
                        label = { Text("Resolved ${state.resolvedCount}") },
                        leadingIcon = if (state.showResolved) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null,
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.events.isEmpty() -> ErrorState(
                message = state.error!!,
                onRetry = viewModel::load,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                val list = state.filtered
                if (list.isEmpty()) {
                    item { EmptyAlerts(state.showResolved, Modifier.fillParentMaxSize()) }
                } else {
                    items(list, key = { it.id }) { event -> AlertCard(event) }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(event: AlertEvent) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg, iconRes) = when (event.severity) {
        AlertSeverity.Error   -> Triple(colors.errorContainer, colors.onErrorContainer, Icons.Default.Warning)
        AlertSeverity.Warning -> Triple(colors.warningContainer, colors.onWarningContainer, Icons.Default.Warning)
        AlertSeverity.Info    -> Triple(colors.surfaceContainerHigh, colors.onSurface, Icons.Default.Info)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = if (event.severity == AlertSeverity.Info) colors.surfaceContainerHighest else bg.copy(alpha = 0.7f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconRes, null, tint = fg, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        event.systemName,
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                        color = fg,
                    )
                    Text(
                        event.created.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = fg.copy(alpha = 0.7f),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${event.metricName} exceeded threshold — value: %.1f".format(event.value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = fg,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.metricName, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = fg.copy(alpha = 0.85f))
                    Text("·", style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.85f))
                    Text("%.1f".format(event.value), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = fg.copy(alpha = 0.85f))
                    if (!event.isActive && !event.resolved.isNullOrBlank()) {
                        Text("·", style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.85f))
                        Text("resolved ${event.resolved!!.take(10)}", style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlerts(showResolved: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("All clear", style = MaterialTheme.typography.titleMedium)
            Text(
                if (showResolved) "No resolved alerts." else "No active alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Failed to load alerts", style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
