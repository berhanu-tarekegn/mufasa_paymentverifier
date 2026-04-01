package com.itechsolution.mufasapay.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.ui.components.EmptyStateView
import com.itechsolution.mufasapay.ui.components.ErrorView
import com.itechsolution.mufasapay.ui.components.LoadingIndicator
import com.itechsolution.mufasapay.ui.components.StatCard
import com.itechsolution.mufasapay.ui.state.UiState
import com.itechsolution.mufasapay.util.BatteryOptimizationUtils
import com.itechsolution.mufasapay.util.DateTimeUtils
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

/**
 * Main dashboard screen showing statistics and recent activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSenders: () -> Unit,
    onNavigateToWebhook: () -> Unit,
    onNavigateToHistory: () -> Unit,
    showScaffold: Boolean = true,
    setupSummary: DashboardSetupSummary = DashboardSetupSummary(),
    viewModel: DashboardViewModel = koinViewModel()
) {
    val statsState by viewModel.statsState.collectAsState()
    val recentMessages by viewModel.recentMessages.collectAsState()

    val content: @Composable (Modifier) -> Unit = { modifier ->
        when (val state = statsState) {
            is UiState.Idle -> {
                // Should not happen as we load on init
            }

            is UiState.Loading -> {
                LoadingIndicator(modifier = modifier)
            }

            is UiState.Success -> {
                DashboardContent(
                    stats = state.data,
                    recentMessages = recentMessages,
                    setupSummary = setupSummary,
                    onNavigateToSenders = onNavigateToSenders,
                    onNavigateToWebhook = onNavigateToWebhook,
                    onNavigateToHistory = onNavigateToHistory,
                    modifier = modifier
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = modifier
                )
            }
        }
    }

    if (showScaffold) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MufasaPay Dashboard") }
                )
            }
        ) { paddingValues ->
            content(Modifier.padding(paddingValues))
        }
    } else {
        content(Modifier)
    }
}

data class DashboardSetupSummary(
    val enabledSenderCount: Int = 0,
    val enabledTemplateCount: Int = 0,
    val webhookReady: Boolean = false
)

@Composable
private fun DashboardContent(
    stats: com.itechsolution.mufasapay.domain.usecase.dashboard.DeliveryStats,
    recentMessages: List<SmsMessage>,
    setupSummary: DashboardSetupSummary,
    onNavigateToSenders: () -> Unit,
    onNavigateToWebhook: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Section
        item {
            Text(
                text = "Operations Snapshot",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SetupOverviewCard(
                setupSummary = setupSummary,
                onNavigateToSenders = onNavigateToSenders,
                onNavigateToWebhook = onNavigateToWebhook,
                onNavigateToHistory = onNavigateToHistory
            )
        }

        // SMS Statistics - Horizontal scrollable row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total SMS",
                    value = stats.totalSms.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Forwarded",
                    value = stats.forwardedSms.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Daily Sum",
                    value = formatAmount(stats.dailyAmountTotal),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Weekly Sum",
                    value = formatAmount(stats.weeklyAmountTotal),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Delivery Statistics
        item {
            Text(
                text = "Delivery Status",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Success",
                    value = stats.successfulDeliveries.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Failed",
                    value = stats.failedDeliveries.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Pending",
                    value = stats.pendingDeliveries.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Retrying",
                    value = stats.retryingDeliveries.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sender Statistics
        item {
            Text(
                text = "Senders",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total Senders",
                    value = stats.totalSenders.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Enabled",
                    value = stats.enabledSenders.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            item {
                BatteryOptimizationSection()
            }
        }

        // Recent Messages Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Messages",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(onClick = onNavigateToHistory) {
                    Text("View All")
                }
            }
        }

        if (recentMessages.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No SMS messages yet. Messages will appear here once received.",
                    modifier = Modifier.height(200.dp)
                )
            }
        } else {
            items(recentMessages.take(3), key = { it.id }) { message ->
                RecentMessageCard(message)
            }
        }
    }
}

@Composable
private fun SetupOverviewCard(
    setupSummary: DashboardSetupSummary,
    onNavigateToSenders: () -> Unit,
    onNavigateToWebhook: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val configurationReady = setupSummary.enabledSenderCount > 0 &&
        setupSummary.enabledTemplateCount > 0 &&
        setupSummary.webhookReady

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (configurationReady) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (configurationReady) "Pipeline Ready" else "Setup Still Needs Attention",
                style = MaterialTheme.typography.titleMedium,
                color = if (configurationReady) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )

            Text(
                text = if (configurationReady) {
                    "${setupSummary.enabledSenderCount} enabled senders, ${setupSummary.enabledTemplateCount} active templates, and a live webhook are in place."
                } else {
                    buildString {
                        if (setupSummary.enabledSenderCount == 0) {
                            append("Add at least one enabled sender. ")
                        }
                        if (setupSummary.enabledTemplateCount == 0) {
                            append("Configure a template with {amount} and {transaction}. ")
                        }
                        if (!setupSummary.webhookReady) {
                            append("Enable and configure the webhook endpoint.")
                        }
                    }.trim()
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (configurationReady) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onNavigateToSenders,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Senders")
                }
                FilledTonalButton(
                    onClick = onNavigateToWebhook,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Webhook")
                }
            }
            if (configurationReady) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("History")
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return "ETB %.2f".format(Locale.getDefault(), amount)
}

@Composable
private fun BatteryOptimizationSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(BatteryOptimizationUtils.isIgnoringBatteryOptimizations(context))
    }

    DisposableEffect(context) {
        val lifecycleOwner = context as? ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBatteryOptimizations =
                    BatteryOptimizationUtils.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    if (isIgnoringBatteryOptimizations) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Battery Optimization Detected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Disable battery optimization for reliable background SMS processing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    BatteryOptimizationUtils.requestIgnoreBatteryOptimizations(context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Text(
                    text = "Disable Optimization",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun RecentMessageCard(
    message: SmsMessage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = DateTimeUtils.formatRelativeTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (message.amount != null || !message.transactionId.isNullOrBlank()) {
                Text(
                    text = buildString {
                        message.amount?.let { append(formatAmount(it)) }
                        if (!message.transactionId.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("Txn: ${message.transactionId}")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = if (message.isForwarded) "Forwarded" else "Not forwarded",
                style = MaterialTheme.typography.labelSmall,
                color = if (message.isForwarded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
