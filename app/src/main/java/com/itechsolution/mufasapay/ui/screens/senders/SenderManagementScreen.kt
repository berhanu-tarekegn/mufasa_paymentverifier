package com.itechsolution.mufasapay.ui.screens.senders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.ui.components.EmptyStateView
import com.itechsolution.mufasapay.ui.components.LoadingIndicator
import com.itechsolution.mufasapay.ui.components.SenderListItem
import com.itechsolution.mufasapay.util.SmsPatternExtractor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Sender management screen - manage enabled/disabled providers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SenderManagementViewModel = koinViewModel()
) {
    val sendersList = viewModel.senders.collectAsState().value
    val showAddSheet by viewModel.showAddDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var senderToDelete by remember { mutableStateOf<String?>(null) }
    var senderToConfigurePattern by remember { mutableStateOf<String?>(null) }

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
                title = { Text("Manage Senders") },
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
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Sender")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            sendersList == null -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            sendersList.isEmpty() -> {
                EmptyStateView(
                    message = "No senders configured. Add a sender to start monitoring SMS.",
                    actionText = "Add Sender",
                    onAction = { viewModel.showAddDialog() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sendersList, key = { it.senderId }) { sender ->
                        SenderListItem(
                            sender = sender,
                            onToggle = { viewModel.toggleSenderStatus(sender.senderId) },
                            onDelete = { senderToDelete = sender.senderId },
                            onConfigurePattern = { senderToConfigurePattern = sender.senderId }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    senderToDelete?.let { senderId ->
        AlertDialog(
            onDismissRequest = { senderToDelete = null },
            title = { Text("Delete Sender") },
            text = { Text("Are you sure you want to delete this sender? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeSender(senderId)
                        senderToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { senderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add sender bottom sheet
    if (showAddSheet) {
        AddSenderBottomSheet(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { senderId, displayName ->
                viewModel.addSender(senderId, displayName, null)
            }
        )
    }

    // Configure pattern bottom sheet
    senderToConfigurePattern?.let { senderId ->
        ConfigurePatternBottomSheet(
            senderId = senderId,
            onDismiss = { senderToConfigurePattern = null },
            onPatternSaved = { pattern ->
                viewModel.updateSenderPattern(senderId, pattern)
                senderToConfigurePattern = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSenderBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var senderId by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Add Sender",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senderId,
                onValueChange = { senderId = it },
                label = { Text("Sender ID") },
                placeholder = { Text("e.g., CBE BIRR") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                placeholder = { Text("e.g., CBE Birr") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You can configure a message pattern after adding the sender.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        onConfirm(senderId.trim(), displayName.trim())
                        sheetState.hide()
                    }
                },
                enabled = senderId.isNotBlank() && displayName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Sender")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurePatternBottomSheet(
    senderId: String,
    onDismiss: () -> Unit,
    onPatternSaved: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var sampleMessage by remember { mutableStateOf("") }
    var extractedPattern by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Configure Pattern",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Paste a sample \"money received\" SMS from $senderId below. The app will extract a pattern to identify similar messages.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = sampleMessage,
                onValueChange = {
                    sampleMessage = it
                    extractedPattern = null
                    error = null
                },
                label = { Text("Sample Message") },
                placeholder = { Text("e.g., You have received ETB 1,500.00 from...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (extractedPattern != null) {
                Text(
                    text = "Extracted pattern:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$extractedPattern\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            onPatternSaved(extractedPattern!!)
                            sheetState.hide()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Pattern")
                }
            } else {
                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = {
                        val pattern = SmsPatternExtractor.extractPattern(sampleMessage)
                        if (pattern != null) {
                            extractedPattern = pattern
                        } else {
                            error = "Could not extract a pattern. Make sure the message contains an amount (e.g., 1,500.00)."
                        }
                    },
                    enabled = sampleMessage.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Extract Pattern")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
