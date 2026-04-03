package com.itechsolution.mufasapay.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.ui.components.EmptyStateView
import com.itechsolution.mufasapay.ui.components.SmsListItem
import org.koin.androidx.compose.koinViewModel

/**
 * SMS history screen - view received messages and retry failed deliveries
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsHistoryScreen(
    onNavigateBack: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: SmsHistoryViewModel = koinViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    var messageToDelete by remember { mutableStateOf<SmsMessage?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Transactions") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (messages.isEmpty()) {
                EmptyStateView(
                    message = when (filter) {
                        FilterType.ALL -> "No transactions yet. Matched SMS transactions will appear here once received."
                        FilterType.FORWARDED -> "No synced transactions yet."
                        FilterType.NEEDS_ACTION -> "No transactions currently need attention."
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TransactionSummaryCard(summary = summary)
                    }

                    // Filter chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = filter == FilterType.ALL,
                                    onClick = { viewModel.setFilter(FilterType.ALL) },
                                    label = { Text("All") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filter == FilterType.FORWARDED,
                                    onClick = { viewModel.setFilter(FilterType.FORWARDED) },
                                    label = { Text("Forwarded") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filter == FilterType.NEEDS_ACTION,
                                    onClick = { viewModel.setFilter(FilterType.NEEDS_ACTION) },
                                    label = { Text("Needs Action") }
                                )
                            }
                        }
                    }

                    // SMS messages
                    items(messages, key = { it.id }) { message ->
                        SmsListItem(
                            message = message,
                            onRetry = if (!message.isForwarded) {
                                { viewModel.retryDelivery(message.id) }
                            } else null,
                            onDelete = { messageToDelete = message }
                        )
                    }
                }
            }
    }

    messageToDelete?.let { message ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Delete Transaction") },
            text = {
                Text(
                    if (message.isForwarded) {
                        "This will delete transaction ${message.transactionId ?: ""} from the server and remove it locally. Daily and weekly totals will be updated."
                    } else {
                        "This will delete the local transaction and recalculate totals."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage(message.id)
                        messageToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TransactionSummaryCard(summary: TransactionSummary) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Transactions Overview",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Track matched SMS transactions, retry failed syncs, and remove stale records from one place.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryMetric(
                    label = "Matched",
                    value = summary.total.toString(),
                    modifier = Modifier.weight(1f)
                )
                HistoryMetric(
                    label = "Synced",
                    value = summary.forwarded.toString(),
                    modifier = Modifier.weight(1f)
                )
                HistoryMetric(
                    label = "Needs Action",
                    value = summary.needsAction.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
