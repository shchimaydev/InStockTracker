package com.ist.instocktracker.feature.linkitem.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NumberWheelPickerDialogTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initialValueRoundTripsThroughDoneUntouched() = runComposeUiTest {
        var doneValue: Int? = null

        setContent {
            NumberWheelPickerDialog(
                range = 1..24,
                initialValue = 4,
                onDismissRequest = {},
                onDone = { doneValue = it }
            )
        }

        onNodeWithText("Done").performClick()

        assertEquals(4, doneValue)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelNeverInvokesOnDone() = runComposeUiTest {
        var doneValue: Int? = null
        var dismissCount = 0

        setContent {
            NumberWheelPickerDialog(
                range = 1..24,
                initialValue = 4,
                onDismissRequest = { dismissCount++ },
                onDone = { doneValue = it }
            )
        }

        onNodeWithText("Cancel").performClick()

        assertNull(doneValue)
        assertEquals(1, dismissCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectingAVisibleRowUpdatesTheCommittedValue() = runComposeUiTest {
        var doneValue: Int? = null

        setContent {
            NumberWheelPickerDialog(
                range = 1..24,
                initialValue = 4,
                onDismissRequest = {},
                onDone = { doneValue = it }
            )
        }

        // "6" sits within the initially visible window around the centered value 4
        // (2 rows either side), so it's guaranteed to be composed without scrolling.
        onNodeWithText("6").performClick()
        onNodeWithText("Done").performClick()

        assertEquals(6, doneValue)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rangeBoundariesAreRespected() = runComposeUiTest {
        var doneValue: Int? = null

        setContent {
            NumberWheelPickerDialog(
                range = 1..24,
                initialValue = 24,
                onDismissRequest = {},
                onDone = { doneValue = it }
            )
        }

        onNodeWithText("25").assertDoesNotExist()

        onNodeWithText("Done").performClick()

        assertEquals(24, doneValue)
    }
}
