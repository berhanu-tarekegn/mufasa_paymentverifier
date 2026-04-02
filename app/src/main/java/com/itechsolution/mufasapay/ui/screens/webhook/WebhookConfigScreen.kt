package com.itechsolution.mufasapay.ui.screens.webhook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import org.koin.androidx.compose.koinViewModel

/**
 * Webhook configuration screen - configure webhook endpoint and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhookConfigScreen(
    onNavigateBack: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: WebhookConfigViewModel = koinViewModel()
) {
    val url by viewModel.url.collectAsState()
    val method by viewModel.method.collectAsState()
    val headers by viewModel.headers.collectAsState()
    val authType by viewModel.authType.collectAsState()
    val authValue by viewModel.authValue.collectAsState()
    val timeout by viewModel.timeout.collectAsState()
    val retryEnabled by viewModel.retryEnabled.collectAsState()
    val maxRetries by viewModel.maxRetries.collectAsState()
    val retryDelayMs by viewModel.retryDelayMs.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
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
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Webhook Configuration") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable/Disable switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Webhook Forwarding",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.updateIsEnabled(it) }
                )
            }

            // URL
            OutlinedTextField(
                value = url,
                onValueChange = { viewModel.updateUrl(it) },
                label = { Text("Webhook Base URL") },
                placeholder = { Text("https://example.com") },
                supportingText = { Text("Uploads use POST /v1/transactions. Deletes use DELETE /v1/transactions/{transaction_id}.") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled
            )

            // Method dropdown
            var methodExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = methodExpanded,
                onExpandedChange = { methodExpanded = it && isEnabled }
            ) {
                OutlinedTextField(
                    value = method,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("HTTP Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = isEnabled
                )
                ExposedDropdownMenu(
                    expanded = methodExpanded,
                    onDismissRequest = { methodExpanded = false }
                ) {
                    listOf("POST").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.updateMethod(item)
                                methodExpanded = false
                            }
                        )
                    }
                }
            }

            // Headers section
            Text(
                text = "Headers",
                style = MaterialTheme.typography.titleSmall
            )

            HeadersSection(
                headers = headers,
                onAddHeader = { key, value -> viewModel.addHeader(key, value) },
                onRemoveHeader = { viewModel.removeHeader(it) },
                enabled = isEnabled
            )

            // Auth type dropdown
            var authTypeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = authTypeExpanded,
                onExpandedChange = { authTypeExpanded = it && isEnabled }
            ) {
                OutlinedTextField(
                    value = authType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Authentication Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = isEnabled
                )
                ExposedDropdownMenu(
                    expanded = authTypeExpanded,
                    onDismissRequest = { authTypeExpanded = false }
                ) {
                    listOf("NONE", "BEARER", "BASIC", "API_KEY").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.updateAuthType(item)
                                authTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Auth value (show only if not NONE)
            if (authType != "NONE") {
                OutlinedTextField(
                    value = authValue,
                    onValueChange = { viewModel.updateAuthValue(it) },
                    label = { Text("Authentication Value") },
                    placeholder = { Text("Token or credentials") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEnabled
                )
            }

            // Timeout
            Text(
                text = "Timeout: ${timeout}ms",
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = timeout.toFloat(),
                onValueChange = { viewModel.updateTimeout(it.toInt()) },
                valueRange = 5000f..60000f,
                steps = 10,
                enabled = isEnabled
            )

            // Retry settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Retry on Failure",
                    style = MaterialTheme.typography.titleSmall
                )
                Switch(
                    checked = retryEnabled,
                    onCheckedChange = { viewModel.updateRetryEnabled(it) },
                    enabled = isEnabled
                )
            }

            if (retryEnabled) {
                Text(
                    text = "Max Retries: $maxRetries",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = maxRetries.toFloat(),
                    onValueChange = { viewModel.updateMaxRetries(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    enabled = isEnabled
                )

                Text(
                    text = "Retry Delay: ${retryDelayMs}ms",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = retryDelayMs.toFloat(),
                    onValueChange = { viewModel.updateRetryDelayMs(it.toLong()) },
                    valueRange = 1000f..30000f,
                    steps = 28,
                    enabled = isEnabled
                )
            }

            // Test result
            testResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = url.isNotBlank() && !isTesting && isEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isTesting) "Testing..." else "Test")
                }

                Button(
                    onClick = { viewModel.saveConfig() },
                    enabled = url.isNotBlank() && !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            }
        }
    }
}

@Composable
private fun HeadersSection(
    headers: Map<String, String>,
    onAddHeader: (String, String) -> Unit,
    onRemoveHeader: (String) -> Unit,
    enabled: Boolean
) {
    var headerKey by remember { mutableStateOf("") }
    var headerValue by remember { mutableStateOf("") }

    // Display existing headers
    headers.forEach { (key, value) ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onRemoveHeader(key) },
                    enabled = enabled
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove header")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Add new header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = headerKey,
            onValueChange = { headerKey = it },
            label = { Text("Key") },
            placeholder = { Text("Content-Type") },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        OutlinedTextField(
            value = headerValue,
            onValueChange = { headerValue = it },
            label = { Text("Value") },
            placeholder = { Text("application/json") },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = {
            onAddHeader(headerKey.trim(), headerValue.trim())
            headerKey = ""
            headerValue = ""
        },
        enabled = headerKey.isNotBlank() && headerValue.isNotBlank() && enabled
    ) {
        Text("Add Header")
    }
}
