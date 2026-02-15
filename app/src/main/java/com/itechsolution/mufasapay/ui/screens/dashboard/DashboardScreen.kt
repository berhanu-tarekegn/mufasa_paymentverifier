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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.ui.components.EmptyStateView
import com.itechsolution.mufasapay.ui.components.ErrorView
import com.itechsolution.mufasapay.ui.components.LoadingIndicator
import com.itechsolution.mufasapay.ui.components.StatCard
import com.itechsolution.mufasapay.ui.state.UiState
import com.itechsolution.mufasapay.util.DateTimeUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Main dashboard screen showing statistics and recent activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSenders: () -> Unit,
    onNavigateToWebhook: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val statsState by viewModel.statsState.collectAsState()
    val recentMessages by viewModel.recentMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MufasaPay Dashboard") }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onNavigateToSenders,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Manage Senders")
                }

                FloatingActionButton(
                    onClick = onNavigateToWebhook,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Configure Webhook")
                }

                FloatingActionButton(
                    onClick = onNavigateToHistory,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.List, contentDescription = "SMS History")
                }
            }
        }
    ) { paddingValues ->
        when (val state = statsState) {
            is UiState.Idle -> {
                // Should not happen as we load on init
            }

            is UiState.Loading -> {
                LoadingIndicator()
            }

            is UiState.Success -> {
                DashboardContent(
                    stats = state.data,
                    recentMessages = recentMessages,
                    onNavigateToHistory = onNavigateToHistory,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = state.message
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    stats: com.itechsolution.mufasapay.domain.usecase.dashboard.DeliveryStats,
    recentMessages: List<SmsMessage>,
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
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
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
