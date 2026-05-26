package com.beszel.android.ui.screens.systems

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.System
import com.beszel.android.data.model.SystemStatus
import com.beszel.android.data.model.formatBytesPerSec
import com.beszel.android.data.model.formatUptime
import com.beszel.android.ui.components.BeszelPullToRefresh
import com.beszel.android.ui.components.LinearMeter
import com.beszel.android.ui.components.StatusDot
import com.beszel.android.ui.theme.success
import com.beszel.android.ui.theme.warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemsScreen(
    viewModel: SystemsViewModel,
    onOpenSystem: (String) -> Unit,
    onAddSystem: () -> Unit,
    compact: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0 } }
    val searchFocus = remember { FocusRequester() }

    Scaffold(
        topBar = {
            Surface(
                color = if (scrolled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
                tonalElevation = if (scrolled) 3.dp else 0.dp,
            ) {
                Column {
                    // Top bar row
                    Row(
                        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.searchActive) {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = viewModel::setSearchQuery,
                                onClose = { viewModel.setSearchActive(false) },
                                focusRequester = searchFocus,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            )
                            LaunchedEffect(Unit) { searchFocus.requestFocus() }
                        } else {
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { viewModel.setSearchActive(true) }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                        }
                    }
                    // Title area (hidden when searching)
                    if (!state.searchActive) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)) {
                            Text("Systems", style = MaterialTheme.typography.headlineMedium)
                            val active = state.systems.filter { it.status == SystemStatus.Up || it.status == SystemStatus.Unknown }
                            if (active.isNotEmpty()) {
                                val avgCpu = active.map { it.cpu }.average().toInt()
                                val avgMem = active.map { it.memPct }.average().toInt()
                                Text(
                                    "${state.systems.size} hosts · avg cpu $avgCpu% · avg mem $avgMem%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                    // Filter chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        val filters = listOf(
                            SystemFilter.All to "All",
                            SystemFilter.Up to "Up",
                            SystemFilter.Warning to "Warning",
                            SystemFilter.Down to "Down",
                            SystemFilter.Paused to "Paused",
                        )
                        items(filters) { (filter, label) ->
                            FilterChip(
                                selected = state.filter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = { Text("$label ${state.countFor(filter)}") },
                                leadingIcon = if (state.filter == filter) {{
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                }} else null,
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSystem,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add system") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { innerPadding ->
        BeszelPullToRefresh(
            refreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.systems.isEmpty() -> ErrorState(
                    message = state.error!!,
                    onRetry = viewModel::load,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val list = state.filtered
                    if (list.isEmpty()) {
                        item {
                            EmptyState(modifier = Modifier.fillParentMaxSize())
                        }
                    } else {
                        items(list, key = { it.id }) { sys ->
                            SystemCard(sys = sys, onClick = { onOpenSystem(sys.id) }, compact = compact)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search systems") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Close, "Clear")
            }
        },
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {}),
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Composable
fun SystemCard(sys: System, onClick: () -> Unit, compact: Boolean = false) {
    val inactive = sys.status == SystemStatus.Down || sys.status == SystemStatus.Paused
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(if (compact) 12.dp else 16.dp)) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(sys.status, size = 10.dp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sys.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        buildString {
                            append(sys.host)
                            if (sys.uptimeSeconds > 0) append(" · ${formatUptime(sys.uptimeSeconds)}")
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.onSurfaceVariant,
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }

            // Metrics (hidden in compact mode or when inactive)
            if (!compact && !inactive) {
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("CPU" to sys.cpu, "MEM" to sys.memPct, "DISK" to sys.diskPct).forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                Text(
                                    "%.0f%%".format(value),
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearMeter(value = value, height = 4.dp)
                        }
                    }
                }

                // Net / load / temp row
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.NetworkCheck, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(
                        "${formatBytesPerSec(sys.netRecvBps)}↓ ${formatBytesPerSec(sys.netSentBps)}↑",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    val load1 = sys.loadAvg.getOrNull(0)
                    if (load1 != null) {
                        Text("·", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "load %.2f".format(load1),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            // Status banner for down/paused
            if (!compact && inactive) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (sys.status == SystemStatus.Down) colors.errorContainer else colors.surfaceContainerHighest,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (sys.status == SystemStatus.Down) Icons.Default.Warning else Icons.Default.Pause,
                            null,
                            tint = if (sys.status == SystemStatus.Down) colors.onErrorContainer else colors.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            if (sys.status == SystemStatus.Down) "Agent not responding" else "Paused — not collecting",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sys.status == SystemStatus.Down) colors.onErrorContainer else colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Storage, null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("No systems match", style = MaterialTheme.typography.titleMedium)
            Text(
                "Try a different filter or search.",
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
            Icon(
                Icons.Default.CloudOff, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("Failed to load systems", style = MaterialTheme.typography.titleMedium)
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
