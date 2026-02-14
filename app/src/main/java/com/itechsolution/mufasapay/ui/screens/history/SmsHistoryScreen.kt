package com.itechsolution.mufasapay.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    viewModel: SmsHistoryViewModel = koinViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

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
            TopAppBar(
                title = { Text("SMS History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (messages.isEmpty()) {
                EmptyStateView(
                    message = when (filter) {
                        FilterType.ALL -> "No SMS messages yet. Messages will appear here once received."
                        FilterType.FORWARDED -> "No forwarded messages."
                        FilterType.NOT_FORWARDED -> "No pending messages."
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
                                    selected = filter == FilterType.NOT_FORWARDED,
                                    onClick = { viewModel.setFilter(FilterType.NOT_FORWARDED) },
                                    label = { Text("Not Forwarded") }
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
                            } else null
                        )
                    }
                }
            }
    }
}
