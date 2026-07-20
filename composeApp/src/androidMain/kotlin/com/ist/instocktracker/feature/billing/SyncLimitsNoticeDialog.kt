package com.ist.instocktracker.feature.billing

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ist.instocktracker.data.LinkItemInfo
import com.ist.instocktracker.data.SyncLimitsResult
import com.ist.instocktracker.utils.capitalizeWords

/**
 * One-time acknowledgment shown after a subscription-tier change causes items to be
 * frozen (over the new limit) or unfrozen (spare capacity became available).
 */
@Composable
fun SyncLimitsNoticeDialog(
    result: SyncLimitsResult,
    onDismiss: () -> Unit
) {
    val isFreeze = result.frozenItems.isNotEmpty()
    val items = if (isFreeze) result.frozenItems else result.unfrozenItems

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (isFreeze) "Some items were paused" else "Items resumed")
        },
        text = {
            Column {
                Text(
                    text = if (isFreeze) {
                        "Your plan changed and can no longer track all of your items. " +
                                "These items have been paused (not deleted):"
                    } else {
                        "Your plan now has room for more items. These paused items have been resumed:"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                items.forEach { item ->
                    Text(
                        text = "• ${item.label?.capitalizeWords() ?: "Untitled item"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK, got it")
            }
        }
    )
}

@Preview
@Composable
private fun SyncLimitsNoticeDialogPreview() {
    val results = SyncLimitsResult(
        trackableItemsLeft = 2,
        frozenItems = listOf(LinkItemInfo(id = "frozen1", label = "Frozen 1")),
//        frozenItems = emptyList(),
        unfrozenItems = listOf(LinkItemInfo(id = "unfrozen1", label = "Unfrozen 1"))
    )
    SyncLimitsNoticeDialog(
        result = results,
        onDismiss = {}
    )
}