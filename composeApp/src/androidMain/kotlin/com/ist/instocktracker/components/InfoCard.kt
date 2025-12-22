package com.ist.instocktracker.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    value: @Composable (() -> Unit)? = null,
    subtitle: String? = null,
    statusColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit) = {}
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (statusColor != null) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (value != null) {
                        value()

                    }
                }

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
fun InfoCardPreview() {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        InfoCard(
            modifier = Modifier.weight(1f),
            title = "Mode",
            value = { Text("In Stock") },
            onClick = {}
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            title = "Start At",
            value = { Text("and there tiwll 12/16/2025 at 10:31") },
            onClick = {}
        )
    }

}

@Preview
@Composable
fun InfoCardLinkPreview() {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        InfoCard(
            title = "Link",
            value = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.padding(end = 6.dp),
                        text = "Link",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2196F3)
                    )
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }

            },
            trailingIcon = {
                Row {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                    }
                }
            },
            onClick = {}
        )
    }

}
