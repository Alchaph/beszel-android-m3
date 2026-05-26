package com.beszel.android.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.*
import com.beszel.android.ui.components.BigChart
import com.beszel.android.ui.components.LinearMeter
import com.beszel.android.ui.components.Sparkline
import com.beszel.android.ui.components.StatusDot
import com.beszel.android.ui.theme.success
import com.beszel.android.ui.theme.warning
import com.beszel.android.ui.theme.warningContainer
import com.beszel.android.ui.theme.onWarningContainer
import com.beszel.android.ui.theme.successContainer
import com.beszel.android.ui.theme.onSuccessContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemDetailScreen(
    system: System,
    viewModel: SystemDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(system) { viewModel.setSystem(system) }

    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                )
                // System header
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatusDot(system.status, size = 8.dp)
                        Text(
                            when (system.status) {
                                SystemStatus.Up      -> "Online"
                                SystemStatus.Down    -> "Offline"
                                SystemStatus.Unknown -> "Warning"
                                SystemStatus.Paused  -> "Paused"
                                SystemStatus.Pending -> "Pending"
                            }.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(system.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        buildString {
                            append(system.host)
                            val d = state.details
                            if (d != null) append(" · ${d.osName} · ${d.cores} cores · ${formatBytes(d.totalMemoryBytes)}")
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Tabs
                val tabs = listOf(
                    DetailTab.System to "System",
                    DetailTab.Docker to "Docker (${state.containers.size})",
                    DetailTab.Info to "Info",
                )
                TabRow(
                    selectedTabIndex = tabs.indexOfFirst { it.first == state.activeTab }.coerceAtLeast(0),
                    containerColor = colors.surface,
                    contentColor = colors.primary,
                ) {
                    tabs.forEach { (tab, label) ->
                        Tab(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.setTab(tab) },
                            text = { Text(label, style = MaterialTheme.typography.titleSmall) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                when (state.activeTab) {
                    DetailTab.System -> systemTab(system, state, viewModel::setTimeRange)
                    DetailTab.Docker -> dockerTab(state.containers, system.status)
                    DetailTab.Info   -> infoTab(system, state.details)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.systemTab(
    system: System,
    state: SystemDetailUiState,
    onSetTimeRange: (TimeRange) -> Unit,
) {
    // Time range chips
    item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TimeRange.entries) { range ->
                FilterChip(
                    selected = state.timeRange == range,
                    onClick = { onSetTimeRange(range) },
                    label = { Text(range.label) },
                    leadingIcon = if (state.timeRange == range) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null,
                )
            }
        }
    }

    // Inactive banner
    val inactive = system.status == SystemStatus.Down || system.status == SystemStatus.Paused
    if (inactive) {
        item { InactiveBanner(system.status) }
    }

    if (state.statsLoading && state.stats.isEmpty()) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
        return
    }

    if (state.stats.isEmpty()) {
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.BarChart, null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("No chart data", style = MaterialTheme.typography.titleSmall)
                    if (state.statsError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.statsError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    } else {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Stats will appear once the agent sends data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
        return
    }

    val cpuData = state.stats.map { it.cpu }
    val memData = state.stats.map { it.memPct }
    val diskData = state.stats.map { it.diskPct }
    val netSentData = state.stats.map { it.netSentBps.toFloat() }
    val netRecvData = state.stats.map { it.netRecvBps.toFloat() }

    // CPU chart
    if (cpuData.isNotEmpty()) {
        item {
            MetricCard(
                label = "CPU",
                value = "%.0f%%".format(system.cpu),
                sub = "load ${system.loadAvg.getOrNull(0)?.let { "%.2f".format(it) } ?: "—"}",
            ) {
                BigChart(data = cpuData)
            }
        }
    }

    // Memory + Disk mini cards
    if (memData.isNotEmpty() || diskData.isNotEmpty()) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (memData.isNotEmpty()) {
                    MiniMetricCard(
                        label = "Memory", value = "%.0f%%".format(system.memPct),
                        trend = memData, modifier = Modifier.weight(1f),
                    )
                }
                if (diskData.isNotEmpty()) {
                    MiniMetricCard(
                        label = "Disk", value = "%.0f%%".format(system.diskPct),
                        trend = diskData, modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // Network chart
    val netMax = (netSentData + netRecvData).maxOrNull()?.let { it * 1.1f }
    if (netSentData.isNotEmpty()) {
        item {
            MetricCard(
                label = "Network",
                value = "${formatBytesPerSec(system.netRecvBps)}↓",
                sub = "${formatBytesPerSec(system.netSentBps)}↑",
            ) {
                BigChart(data = netRecvData, maxOverride = netMax, fillStrong = true)
            }
        }
    }

    // Temperature
    val temps = state.stats.lastOrNull()?.temperatures
    val maxTemp = temps?.values?.maxOrNull()
    if (maxTemp != null) {
        item { TemperatureCard(temp = maxTemp) }
    }
}

@Composable
private fun MetricCard(label: String, value: String, sub: String, chart: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace))
                }
                Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        chart()
    }
}

@Composable
private fun MiniMetricCard(label: String, value: String, trend: List<Float>, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace))
            Spacer(Modifier.height(8.dp))
            Sparkline(data = trend, color = MaterialTheme.colorScheme.tertiary, height = 36f)
        }
    }
}

@Composable
private fun TemperatureCard(temp: Float) {
    val colors = MaterialTheme.colorScheme
    val hot = temp > 65f
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerHigh) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Thermostat, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Temperature", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Text("%.0f°C".format(temp), style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace))
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = if (hot) colors.warningContainer else colors.successContainer,
            ) {
                Text(
                    if (hot) "Hot" else "Normal",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hot) colors.onWarningContainer else colors.onSuccessContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun InactiveBanner(status: SystemStatus) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (status == SystemStatus.Down) colors.errorContainer else colors.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (status == SystemStatus.Down) Icons.Default.Warning else Icons.Default.Pause,
                null,
                tint = if (status == SystemStatus.Down) colors.onErrorContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    if (status == SystemStatus.Down) "Agent not responding" else "Monitoring paused",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (status == SystemStatus.Down) colors.onErrorContainer else colors.onSurface,
                )
                Text(
                    if (status == SystemStatus.Down) "Showing last known values. Check the agent on the host."
                    else "Showing last known values. Resume from hub settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (status == SystemStatus.Down) colors.onErrorContainer else colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.dockerTab(
    containers: List<Container>,
    systemStatus: SystemStatus,
) {
    if (containers.isEmpty()) {
        item {
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox, null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No containers", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (systemStatus == SystemStatus.Down) "Host is offline." else "No Docker daemon detected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        return
    }
    // Summary card
    item {
        val totalCpu = containers.sumOf { it.cpu.toDouble() }.toFloat()
        val totalMem = containers.sumOf { it.memoryBytes }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SummaryMetric("Containers", "${containers.size}")
                SummaryMetric("CPU", "%.1f%%".format(totalCpu))
                SummaryMetric("Memory", formatBytes(totalMem))
            }
        }
    }
    items(containers, key = { it.id }) { ct -> ContainerCard(ct) }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun ContainerCard(ct: Container) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusDot(
                    status = if (ct.status == "running") SystemStatus.Up else SystemStatus.Down,
                    size = 8.dp,
                )
                Text(
                    ct.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    ct.image,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CPU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.1f%%".format(ct.cpu), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                    }
                    LinearMeter(value = (ct.cpu * 2f).coerceAtMost(100f), height = 4.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("MEM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatBytes(ct.memoryBytes), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                    }
                    LinearMeter(
                        value = ((ct.memoryBytes / (1024f * 1024f * 1024f)) * 10f).coerceAtMost(100f),
                        color = MaterialTheme.colorScheme.tertiary,
                        height = 4.dp,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.infoTab(
    system: System,
    details: SystemDetails?,
) {
    val rows = buildList {
        add("Hostname" to (details?.hostname ?: system.name))
        add("Address" to system.host)
        add("Port" to system.port)
        details?.let {
            add("Operating system" to it.osName)
            if (it.kernel.isNotBlank()) add("Kernel" to it.kernel)
            add("CPU model" to it.cpuModel)
            add("Architecture" to it.arch)
            add("CPU cores" to "${it.cores}")
            add("CPU threads" to "${it.threads}")
            add("Total memory" to formatBytes(it.totalMemoryBytes))
            add("Container runtime" to if (it.usingPodman) "Podman" else "Docker")
        }
        if (system.agentVersion.isNotBlank()) add("Agent version" to "beszel-agent ${system.agentVersion}")
        add("Last seen" to system.updated)
    }

    item {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            rows.forEachIndexed { i, (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        k,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(110.dp),
                    )
                    Text(
                        v,
                        style = MaterialTheme.typography.bodyMedium.let {
                            if (k == "Address" || k == "Agent version" || k == "Kernel") it.copy(fontFamily = FontFamily.Monospace) else it
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (i < rows.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
