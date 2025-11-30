package com.ist.instocktracker.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.ist.instocktracker.R
import com.ist.instocktracker.data.*
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController
import com.ist.instocktracker.utils.capitalizeWords

@Composable
fun LinkItemCard(linkItem: LinkItem) {
    val nav = LocalNavController.current
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
            .padding(vertical = 4.dp)
            .clickable {
                nav.navigate("${AppRoutes.DETAILS_LINK_ITEM}?linkItemId=${linkItem.id}")
            },
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
                    text = linkItem.label?.capitalizeWords() ?: "No label",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E)
                )
                LinkItemSubLabelsSection(subLabels = subLabels)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun LinkItemCardPreview() {
    val mockNavController = rememberNavController()
    val sampleLinkItems = listOf(
        LinkItem(
            id = "1",
            label = "playStation 5",
            link = "https://example.com/ps5",
            mode = Mode.IN_STOCK,
            interval = Interval(unit = 2, duration = DurationUnit.HOURS),
            lastCheckResult = true,
            isActive = true
        ),
        LinkItem(
            id = "2",
            label = "Xbox Series X",
            link = "https://example.com/xbox",
            mode = Mode.PRE_ORDER,
            interval = Interval(unit = 30, duration = DurationUnit.MINUTES),
            lastCheckResult = false,
            isActive = true
        ),
        LinkItem(
            id = "3",
            label = "Nintendo Switch OLED",
            link = "https://example.com/switch",
            mode = Mode.OUT_OF_STOCK,
            interval = Interval(unit = 1, duration = DurationUnit.DAYS),
            lastCheckResult = null,
            isActive = false
        )
    )

    CompositionLocalProvider(LocalNavController provides mockNavController) {
        Column {
            sampleLinkItems.forEach { linkItem ->
                LinkItemCard(linkItem = linkItem)
            }
        }
    }

}
