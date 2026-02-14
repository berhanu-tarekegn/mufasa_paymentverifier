package com.itechsolution.mufasapay.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.ui.components.LoadingIndicator
import com.itechsolution.mufasapay.ui.components.ProviderCard
import org.koin.androidx.compose.koinViewModel

/**
 * Provider selection screen - first-run onboarding to select payment providers to monitor
 */
@Composable
fun ProviderSelectionScreen(
    onComplete: () -> Unit,
    viewModel: ProviderSelectionViewModel = koinViewModel()
) {
    val providers by viewModel.providers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCount = remember(providers) {
        providers.count { it.isSelected }
    }
    val saveComplete by viewModel.saveComplete.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to dashboard when save completes
    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onComplete()
        }
    }

    // Show error message in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Select Payment Providers",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = "Choose which Ethiopian payment providers you want to monitor for SMS notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )


                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$selectedCount provider${if (selectedCount != 1) "s" else ""} selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Provider list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(providers, key = { it.senderId }) { provider ->
                        ProviderCard(
                            provider = provider,
                            onToggle = { viewModel.toggleProvider(provider.senderId) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Button(
                    onClick = { viewModel.saveSelections() },
                    enabled = selectedCount > 0 &&!isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with $selectedCount Provider${if (selectedCount != 1) "s" else ""}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.skip() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now")
                }
            }
        }
    }
}
