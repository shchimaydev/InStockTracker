package com.ist.instocktracker.feature.billing

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.ist.instocktracker.data.LinkItemInfo
import com.ist.instocktracker.data.SyncLimitsResult
import kotlin.test.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncLimitsNoticeDialogTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersUnfreezeNoticeWhenNoItemsAreFrozen() = runComposeUiTest {
        val result = SyncLimitsResult(
            trackableItemsLeft = 5,
            frozenItems = emptyList(),
            unfrozenItems = emptyList()
        )

        setContent {
            SyncLimitsNoticeDialog(result = result, onDismiss = {})
        }

        onNodeWithText("Items resumed").assertExists()
        onNodeWithText("OK, got it").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersFrozenItemsWhenPresentInResult() = runComposeUiTest {
        val result = SyncLimitsResult(
            trackableItemsLeft = 0,
            frozenItems = listOf(
                LinkItemInfo(id = "1", label = "some item"),
                LinkItemInfo(id = "2", label = null)
            ),
            unfrozenItems = emptyList()
        )

        setContent {
            SyncLimitsNoticeDialog(result = result, onDismiss = {})
        }

        onNodeWithText("Some items were paused").assertExists()
        onNodeWithText("• Some Item").assertExists()
        onNodeWithText("• Untitled item").assertExists()
    }
}
