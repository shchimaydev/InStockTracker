package com.ist.instocktracker.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ist.instocktracker.PrimaryTint06
import com.ist.instocktracker.PrimaryTint18
import com.ist.instocktracker.PrimaryTint55
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.getDisplayName

fun lastCheckIntoLabel(lastCheckResult: Boolean): String {
    return when (lastCheckResult) {
        true -> "Successful"
        false -> "Unsuccessful"
    }
}

/**
 * Pill chip used on the link card, matching the Figma design system:
 * primary-tinted fill ([PrimaryTint06]) with a hairline primary border
 * ([PrimaryTint18]), 8dp corners, and medium primary-tinted label text.
 *
 * The label wraps at word boundaries (never mid-word); when the chip itself
 * is too wide for the current line, the enclosing [FlowRow] moves it down.
 */
@Composable
fun LinkItemChip(chipValue: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(PrimaryTint06)
            .border(BorderStroke(1.dp, PrimaryTint18), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = chipValue,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            color = PrimaryTint55
        )
    }
}

@Composable
fun LinkItemAdditionalInfo(linkItem: LinkItem) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LinkItemChip(linkItem.mode.displayName)
        LinkItemChip(linkItem.interval.getDisplayName())
    }
}