package com.itechsolution.mufasapay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itechsolution.mufasapay.domain.model.DeliveryStatus

/**
 * Badge component for displaying delivery status
 */
@Composable
fun DeliveryStatusBadge(
    status: DeliveryStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        DeliveryStatus.SUCCESS -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "Success"
        )
        DeliveryStatus.FAILED -> Triple(
            Color(0xFFF44336),
            Color.White,
            "Failed"
        )
        DeliveryStatus.PENDING -> Triple(
            Color(0xFFFF9800),
            Color.White,
            "Pending"
        )
        DeliveryStatus.RETRYING -> Triple(
            Color(0xFF2196F3),
            Color.White,
            "Retrying"
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
