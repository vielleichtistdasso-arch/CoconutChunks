package com.coconutchunks.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coconutchunks.app.data.ReviewStatus

@Composable
fun StatusBadge(status: ReviewStatus, compact: Boolean = false) {
    val (container, content) = when (status) {
        ReviewStatus.REVIEW -> CoconutReviewSoft to CoconutReview
        ReviewStatus.SPECIAL -> CoconutSpecialSoft to CoconutSpecial
        ReviewStatus.MASTERED -> CoconutMasteredSoft to CoconutMastered
    }
    Surface(
        modifier = Modifier.semantics {
            contentDescription = "Review status: ${
                status.name.lowercase().replaceFirstChar { it.uppercase() }
            }"
        },
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            status.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(
                horizontal = if (compact) 9.dp else 11.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    icon: ImageVector = Icons.Default.Spa,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(18.dp).size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun MetricCard(label: String, value: Int, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
