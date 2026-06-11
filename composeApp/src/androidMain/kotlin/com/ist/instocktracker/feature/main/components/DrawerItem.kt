package com.ist.instocktracker.feature.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val DrawerItemHeight = 46.dp
private val DrawerItemShape = RoundedCornerShape(24.dp)
private val DrawerSelectedBg = Color.White.copy(alpha = 0.12f)
private val DrawerSelectedContent = Color.White
private val DrawerUnselectedContent = Color.White.copy(alpha = 0.6f)

/**
 * A single navigation row in the sidebar — icon in a fixed 40dp slot followed by
 * the label, inside a 46dp pill that fills the available width.
 */
@Composable
internal fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    contentColor: Color = if (selected) DrawerSelectedContent else DrawerUnselectedContent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(DrawerItemHeight)
            .clip(DrawerItemShape)
            .background(if (selected) DrawerSelectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(40.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = contentColor,
            style = if (selected) {
                MaterialTheme.typography.titleSmall   // 15sp Bold
            } else {
                MaterialTheme.typography.bodyLarge     // 15sp Regular
            }
        )
    }
}
