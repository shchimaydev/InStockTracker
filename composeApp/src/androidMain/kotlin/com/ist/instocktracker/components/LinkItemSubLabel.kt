package com.ist.instocktracker.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LinkItemSubLabelItem(
    val title: String,
    val value: String
)

fun lastCheckIntoLabel(lastCheckResult: Boolean): String {
    return when (lastCheckResult) {
        true -> "Successful"
        false -> "Unsuccessful"
    }
}

@Composable
fun LinkItemSubLabel(subLabelItem: LinkItemSubLabelItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = subLabelItem.title,
            fontWeight = FontWeight.ExtraLight,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = subLabelItem.value,
            color = MaterialTheme.colorScheme.secondary,
            lineHeight = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }


}

@Composable
fun LinkItemSubLabelsSection(subLabels: List<LinkItemSubLabelItem>): Unit {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        subLabels.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinkItemSubLabel(item)

                if (index < subLabels.lastIndex) {
                    Text(
                        text = "·",
                        lineHeight = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = Color.Gray
                    )
                }
            }


        }
    }
}