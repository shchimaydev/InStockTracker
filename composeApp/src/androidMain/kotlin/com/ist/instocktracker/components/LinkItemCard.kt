package com.ist.instocktracker.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ist.instocktracker.R
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.getDisplayName
import com.ist.instocktracker.utils.LocalNavController

@Composable
fun LinkItemCard(linkItem: LinkItem) {
    val nav = LocalNavController.current

    //linkItem.placeholderImage
    val imageRes = R.drawable.placeholder_image

    val subLabels = buildList {
        add(LinkItemSubLabelItem(title = "Checking", value = linkItem.mode.displayName))
        add(LinkItemSubLabelItem(title = "Runs", value = linkItem.interval.getDisplayName()))

        linkItem.lastCheckResult?.let {
            add(LinkItemSubLabelItem(title = "Last check was", value = lastCheckIntoLabel(it)))
        }
    }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = linkItem.label ?: "No label",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E2E2E)
                )
                LinkItemSubLabelsSection(subLabels = subLabels)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

