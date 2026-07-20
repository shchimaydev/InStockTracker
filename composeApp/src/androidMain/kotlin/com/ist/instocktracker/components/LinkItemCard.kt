package com.ist.instocktracker.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.ist.instocktracker.BorderHairline
import com.ist.instocktracker.InkFaint
import com.ist.instocktracker.InkMuted
import com.ist.instocktracker.R
import com.ist.instocktracker.White
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.navigation.Route
import com.ist.instocktracker.utils.LocalNavController
import com.ist.instocktracker.utils.capitalizeWords

@Composable
fun LinkItemCard(linkItem: LinkItem) {
    val nav = LocalNavController.current
    val imageRes = R.drawable.placeholder_image
    val isFrozen = linkItem.isFrozen

    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .let {
            if (isFrozen) it else it.clickable {
                nav.navigate(Route.LinkItemDetails(linkItem.id))
            }
        }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isFrozen) InkFaint else White),
        elevation = CardDefaults.cardElevation(if (isFrozen) 0.dp else 1.dp),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isFrozen) {
                    Text(
                        text = "Paused",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = InkMuted
                    )
                }
                Text(
                    text = linkItem.label?.capitalizeWords() ?: "No label",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isFrozen) InkMuted else MaterialTheme.colorScheme.onSurface
                )
                LinkItemAdditionalInfo(linkItem, dimmed = isFrozen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val grayscaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (isFrozen) grayscaleFilter else null
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
            userId = "user1",
            label = "playStation 5",
            link = "https://example.com/ps5",
            mode = Mode.IN_STOCK,
            interval = Interval(unit = 2, duration = DurationUnit.HOURS),
            lastCheckResult = true,
            isActive = true
        ),
        LinkItem(
            id = "2",
            userId = "user1",
            label = "Xbox Series X",
            link = "https://example.com/xbox",
            mode = Mode.PRE_ORDER,
            interval = Interval(unit = 30, duration = DurationUnit.MINUTES),
            lastCheckResult = false,
            isActive = true
        ),
        LinkItem(
            id = "3",
            userId = "user1",
            label = "Nintendo Switch OLED",
            link = "https://example.com/switch",
            mode = Mode.OUT_OF_STOCK,
            interval = Interval(unit = 1, duration = DurationUnit.DAYS),
            lastCheckResult = null,
            isActive = false
        ),
        LinkItem(
            id = "4",
            userId = "user1",
            label = "Steam Deck OLED",
            link = "https://example.com/steamdeck",
            mode = Mode.IN_STOCK,
            interval = Interval(unit = 1, duration = DurationUnit.HOURS),
            lastCheckResult = true,
            isActive = true,
            isFrozen = true
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
