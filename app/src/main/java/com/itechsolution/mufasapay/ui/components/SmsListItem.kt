package com.itechsolution.mufasapay.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.util.DateTimeUtils

/**
 * List item component for SMS history
 */
@Composable
fun SmsListItem(
    message: SmsMessage,
    onRetry: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Sender and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(isForwarded = message.isForwarded)
            }

            // Message content
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (message.amount != null || !message.transactionId.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    message.amount?.let {
                        MetaBlock(
                            label = "Amount",
                            value = "ETB %.2f".format(it),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!message.transactionId.isNullOrBlank()) {
                        MetaBlock(
                            label = "Transaction ID",
                            value = message.transactionId ?: "",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Footer: Status and retry button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (message.isForwarded) {
                        "Synced ${message.forwardedAt?.let { "• ${DateTimeUtils.formatRelativeTime(it)}" } ?: ""}"
                    } else {
                        "Needs attention • received ${DateTimeUtils.formatRelativeTime(message.timestamp)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isForwarded) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                if (!message.isForwarded && onRetry != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onRetry) {
                            Text("Retry")
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete message",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete message",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(isForwarded: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isForwarded) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(
            text = if (isForwarded) "Synced" else "Needs Action",
            style = MaterialTheme.typography.labelSmall,
            color = if (isForwarded) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MetaBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
