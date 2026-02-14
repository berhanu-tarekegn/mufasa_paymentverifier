package com.itechsolution.mufasapay.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.ui.util.PermissionUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Permission request screen - first screen if permissions not granted
 */
@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit,
    viewModel: PermissionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val smsPermissionGranted by viewModel.smsPermissionGranted.collectAsState()
    val notificationPermissionGranted by viewModel.notificationPermissionGranted.collectAsState()

    // SMS permissions launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.updateSmsPermissionStatus(allGranted)
    }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updateNotificationPermissionStatus(granted)
    }

    // Check initial permission status
    LaunchedEffect(Unit) {
        viewModel.updateSmsPermissionStatus(PermissionUtils.hasSmsPermissions(context))
        viewModel.updateNotificationPermissionStatus(PermissionUtils.hasNotificationPermission(context))
    }

    // Navigate to next screen if all permissions granted
    LaunchedEffect(smsPermissionGranted, notificationPermissionGranted) {
        if (PermissionUtils.hasAllPermissions(context)) {
            onPermissionsGranted()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.W400,

                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MufasaPay needs these permissions to forward payment SMS notifications",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W400),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.56f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SMS Permission Card
            PermissionCard(
                icon = { Icon(Icons.Default.Email, contentDescription = null) },
                title = "SMS Access",
                description = "Required to read and monitor payment SMS from banks and mobile money providers",
                granted = smsPermissionGranted,
                onRequest = {
                    smsPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Permission Card (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    title = "Notifications",
                    description = "Required to show notification when SMS are forwarded",
                    granted = notificationPermissionGranted,
                    onRequest = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = {
                    if (!smsPermissionGranted) {
                        smsPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                            )
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                enabled = !PermissionUtils.hasAllPermissions(context),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) {
                MaterialTheme.colorScheme.tertiary.copy(0.2f)
            } else {
                MaterialTheme.colorScheme.onTertiary
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            icon()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (granted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Granted",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
