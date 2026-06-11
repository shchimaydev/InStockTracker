package com.ist.instocktracker.feature.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ist.instocktracker.BorderHairline
import com.ist.instocktracker.Cobalt
import com.ist.instocktracker.InkMuted
import com.ist.instocktracker.White
import com.ist.instocktracker.feature.main.LinkFilter

/**
 * Segmented pill control for filtering the main list by [LinkFilter].
 *
 * Styled per the Figma design system: the selected pill is filled with [Cobalt]
 * and white text; unselected pills are outlined with a hairline border and muted text.
 */
@Composable
fun LinkFilterTabs(
    selected: LinkFilter,
    onSelect: (LinkFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinkFilter.entries.forEach { filter ->
            FilterPill(
                label = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    val base = Modifier
        .clip(shape)
        .clickable(onClick = onClick)

    val styled = if (selected) {
        base.background(Cobalt, shape)
    } else {
        base.border(BorderStroke(1.dp, BorderHairline), shape)
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) White else InkMuted,
        modifier = styled.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview
@Composable
private fun LinkFilterTabsPreview() {
    LinkFilterTabs(
        selected = LinkFilter.ACTIVE,
        onSelect = {},
        modifier = Modifier.padding(16.dp)
    )
}
