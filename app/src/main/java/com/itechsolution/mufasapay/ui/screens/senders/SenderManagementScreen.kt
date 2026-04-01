package com.itechsolution.mufasapay.ui.screens.senders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.domain.model.SenderTemplate
import com.itechsolution.mufasapay.ui.components.EmptyStateView
import com.itechsolution.mufasapay.ui.components.LoadingIndicator
import com.itechsolution.mufasapay.ui.components.SenderListItem
import com.itechsolution.mufasapay.util.SmsPatternExtractor
import org.koin.androidx.compose.koinViewModel

/**
 * Sender management screen - manage enabled/disabled providers and their templates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderManagementScreen(
    onNavigateBack: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: SenderManagementViewModel = koinViewModel()
) {
    val sendersList = viewModel.senders.collectAsState().value
    val showAddSheet by viewModel.showAddDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val templates by viewModel.selectedSenderTemplates.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var senderToDelete by remember { mutableStateOf<String?>(null) }
    var senderToManageTemplates by remember { mutableStateOf<String?>(null) }

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
                    title = { Text("Manage Senders") },
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
                            onManageTemplates = {
                                senderToManageTemplates = sender.senderId
                                viewModel.loadTemplatesForSender(sender.senderId)
                            }
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
            text = { Text("Are you sure you want to delete this sender and all its templates? This action cannot be undone.") },
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
                viewModel.addSender(senderId, displayName)
            }
        )
    }

    // Manage templates bottom sheet
    senderToManageTemplates?.let { senderId ->
        ManageTemplatesBottomSheet(
            senderId = senderId,
            templates = templates,
            onDismiss = {
                senderToManageTemplates = null
                viewModel.clearSelectedSender()
            },
            onAddTemplate = { label, pattern ->
                viewModel.addTemplate(senderId, label, pattern)
            },
            onUpdateTemplate = { templateId, label, pattern, isEnabled ->
                viewModel.updateTemplate(templateId, senderId, label, pattern, isEnabled)
            },
            onToggleTemplate = { template ->
                viewModel.toggleTemplateEnabled(template)
            },
            onRemoveTemplate = { templateId ->
                viewModel.removeTemplate(templateId)
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
                text = "You can add message templates after creating the sender.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onConfirm(senderId.trim(), displayName.trim())
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
private fun ManageTemplatesBottomSheet(
    senderId: String,
    templates: List<SenderTemplate>,
    onDismiss: () -> Unit,
    onAddTemplate: (label: String, pattern: String) -> Unit,
    onUpdateTemplate: (templateId: Long, label: String, pattern: String, isEnabled: Boolean) -> Unit,
    onToggleTemplate: (SenderTemplate) -> Unit,
    onRemoveTemplate: (templateId: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddForm by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<SenderTemplate?>(null) }

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
                text = "Message Templates",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Templates define which SMS formats this sender should match. Include {amount} and {transaction} so the app can extract webhook fields and sums.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sender: $senderId",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Existing templates list
            if (templates.isEmpty()) {
                Text(
                    text = "No templates configured yet. Add one below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                templates.forEach { template ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (template.isEnabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (template.isEnabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = template.pattern,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Switch(
                                checked = template.isEnabled,
                                onCheckedChange = { onToggleTemplate(template) }
                            )
                            IconButton(onClick = { editingTemplate = template }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit template",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onRemoveTemplate(template.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove template",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Add template form (toggle)
            if (editingTemplate != null) {
                AddTemplateForm(
                    initialLabel = editingTemplate!!.label,
                    initialPattern = editingTemplate!!.pattern,
                    actionLabel = "Save",
                    onSubmit = { label, pattern ->
                        onUpdateTemplate(
                            editingTemplate!!.id,
                            label,
                            pattern,
                            editingTemplate!!.isEnabled
                        )
                        editingTemplate = null
                    },
                    onCancel = { editingTemplate = null }
                )
            } else if (!showAddForm) {
                OutlinedButton(
                    onClick = { showAddForm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Template")
                }
            } else {
                AddTemplateForm(
                    onSubmit = { label, pattern ->
                        onAddTemplate(label, pattern)
                        showAddForm = false
                    },
                    onCancel = { showAddForm = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddTemplateForm(
    initialLabel: String = "",
    initialPattern: String = "",
    actionLabel: String = "Add",
    onSubmit: (label: String, pattern: String) -> Unit,
    onCancel: () -> Unit
) {
    var label by remember(initialLabel) { mutableStateOf(initialLabel) }
    var patternText by remember(initialPattern) { mutableStateOf(initialPattern) }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            text = "New Template",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label *") },
            placeholder = { Text("e.g., Deposit, Transfer Received") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = patternText,
            onValueChange = {
                patternText = it
                error = null
            },
            label = { Text("Message Pattern *") },
            placeholder = { Text("e.g., Dear {name}, you have received ETB {amount}...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Required: {amount}, {transaction}. Optional: {name}, {account}, {datetime}, {balance}, {ignore}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto-generate button
        OutlinedButton(
            onClick = {
                val extracted = SmsPatternExtractor.extractPattern(patternText)
                if (extracted != null) {
                    patternText = extracted
                } else {
                    error = "Could not auto-generate. Please add placeholders like {amount} manually."
                }
            },
            enabled = patternText.isNotBlank() && !patternText.contains("{"),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Auto-Generate from Sample")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onSubmit(label.trim(), patternText.trim()) },
                enabled = label.isNotBlank() && patternText.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(actionLabel)
            }
        }
    }
}
