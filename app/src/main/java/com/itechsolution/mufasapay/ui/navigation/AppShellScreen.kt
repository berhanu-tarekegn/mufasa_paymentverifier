package com.itechsolution.mufasapay.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.ui.screens.dashboard.DashboardScreen
import com.itechsolution.mufasapay.ui.screens.dashboard.DashboardSetupSummary
import com.itechsolution.mufasapay.ui.screens.history.SmsHistoryScreen
import com.itechsolution.mufasapay.ui.screens.senders.SenderManagementScreen
import com.itechsolution.mufasapay.ui.screens.webhook.WebhookConfigScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class DrawerDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val section: String,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShellScreen(
    senderRepository: SenderRepository = koinInject(),
    webhookRepository: WebhookRepository = koinInject()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val senders by senderRepository.getAllSendersFlow().collectAsState(initial = emptyList())
    val webhookConfig by webhookRepository.getConfigFlow().collectAsState(initial = null)

    val enabledSenderCount = senders.count { it.isEnabled }
    val enabledTemplateCount = senders.sumOf { sender -> sender.templates.count { it.isEnabled } }
    val webhookReady = webhookConfig?.let {
        it.isEnabled && it.uploadUrl.isNotBlank() && it.deleteUrlTemplate.isNotBlank()
    } == true
    val setupReady = enabledSenderCount > 0 && enabledTemplateCount > 0 && webhookReady

    val destinations = listOf(
        DrawerDestination(
            route = Screen.Dashboard.route,
            label = "Overview",
            icon = Icons.Default.Home,
            section = "Operations"
        ),
        DrawerDestination(
            route = Screen.SmsHistory.route,
            label = "Transactions",
            icon = Icons.AutoMirrored.Filled.List,
            section = "Operations"
        ),
        DrawerDestination(
            route = Screen.SenderManagement.route,
            label = "Senders & Templates",
            icon = Icons.Default.AccountCircle,
            section = "Setup",
            badge = enabledSenderCount.takeIf { it > 0 }?.toString()
        ),
        DrawerDestination(
            route = Screen.WebhookConfig.route,
            label = "Webhook Delivery",
            icon = Icons.Default.Build,
            section = "Setup",
            badge = if (webhookReady) "Ready" else "Off"
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp)
            ) {
                DrawerHeader(
                    setupReady = setupReady,
                    enabledSenderCount = enabledSenderCount,
                    enabledTemplateCount = enabledTemplateCount,
                    webhookReady = webhookReady
                )

                val sections = destinations.groupBy { it.section }
                sections.forEach { (section, items) ->
                    Text(
                        text = section,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )

                    items.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                    drawerState.close()
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            badge = {
                                item.badge?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(routeTitle(currentDestination?.route))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open navigation")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToSenders = {
                            navController.navigate(Screen.SenderManagement.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToWebhook = {
                            navController.navigate(Screen.WebhookConfig.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToHistory = {
                            navController.navigate(Screen.SmsHistory.route) {
                                launchSingleTop = true
                            }
                        },
                        showScaffold = false,
                        setupSummary = DashboardSetupSummary(
                            enabledSenderCount = enabledSenderCount,
                            enabledTemplateCount = enabledTemplateCount,
                            webhookReady = webhookReady
                        )
                    )
                }

                composable(Screen.SmsHistory.route) {
                    SmsHistoryScreen(
                        onNavigateBack = {},
                        showTopBar = false
                    )
                }

                composable(Screen.SenderManagement.route) {
                    SenderManagementScreen(
                        onNavigateBack = {},
                        showTopBar = false
                    )
                }

                composable(Screen.WebhookConfig.route) {
                    WebhookConfigScreen(
                        onNavigateBack = {},
                        showTopBar = false
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(
    setupReady: Boolean,
    enabledSenderCount: Int,
    enabledTemplateCount: Int,
    webhookReady: Boolean
) {
    Surface(
        color = if (setupReady) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "MufasaPay",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (setupReady) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (setupReady) {
                    "Ready to ingest, sync, and manage payment transactions"
                } else {
                    "Finish setup, then monitor transactions from the operations pages"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (setupReady) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusMetric(
                    label = "Senders",
                    value = enabledSenderCount.toString()
                )
                StatusMetric(
                    label = "Templates",
                    value = enabledTemplateCount.toString()
                )
                StatusMetric(
                    label = "Webhook",
                    value = if (webhookReady) "On" else "Off"
                )
            }
        }
    }
}

@Composable
private fun StatusMetric(
    label: String,
    value: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun routeTitle(route: String?): String {
    return when (route) {
        Screen.Dashboard.route -> "Operations Overview"
        Screen.SmsHistory.route -> "Transactions"
        Screen.SenderManagement.route -> "Senders & Templates"
        Screen.WebhookConfig.route -> "Webhook Delivery"
        else -> "MufasaPay"
    }
}
