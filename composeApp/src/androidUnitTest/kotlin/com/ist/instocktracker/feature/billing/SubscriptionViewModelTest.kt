package com.ist.instocktracker.feature.billing

import com.ist.instocktracker.Api
import com.ist.instocktracker.CustomerInfoWrapper
import com.ist.instocktracker.billing.RevenueCatManager
import com.ist.instocktracker.data.LinkItemInfo
import com.ist.instocktracker.data.SubscriptionTier
import com.ist.instocktracker.data.SyncLimitsResult
import com.ist.instocktracker.services.ServiceLocator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private data class FakeCustomerInfo(
    override val activeEntitlements: Set<String> = emptySet(),
    override val isPremium: Boolean = false,
    override val activeSubscriptions: Set<String> = emptySet(),
) : CustomerInfoWrapper

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SubscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val customerInfoFlow = MutableStateFlow<CustomerInfoWrapper?>(null)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(RevenueCatManager)
        every { RevenueCatManager.customerInfo } returns customerInfoFlow
        ServiceLocator.api = mockk<Api>(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): SubscriptionViewModel {
        val viewModel = SubscriptionViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    @Test
    fun initialLoadSuccessUpdatesState() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo(
            activeEntitlements = setOf("premium"),
            isPremium = true,
            activeSubscriptions = setOf("sub_5_items")
        )

        val viewModel = createViewModel()
        val state = viewModel.subscriptionState.value

        assertTrue(state.isSubscribed)
        assertTrue(state.isPremium)
        assertEquals(SubscriptionTier.BASE, state.tier)
        assertEquals(setOf("sub_5_items"), state.activeSubscriptions)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun initialLoadReturningNullSetsError() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null

        val viewModel = createViewModel()
        val state = viewModel.subscriptionState.value

        assertEquals("Failed to load subscription status", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun initialLoadThrowingSetsErrorToExceptionMessage() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } throws RuntimeException("boom")

        val viewModel = createViewModel()
        val state = viewModel.subscriptionState.value

        assertEquals("boom", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun isSubscribedTracksEntitlementsWhileTierTracksSubscriptions() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo(
            activeEntitlements = emptySet(),
            activeSubscriptions = setOf("sub_3_items")
        )

        val viewModel = createViewModel()
        val state = viewModel.subscriptionState.value

        assertFalse(state.isSubscribed)
        assertEquals(SubscriptionTier.START, state.tier)
    }

    @Test
    fun tierChangeTriggersLimitSyncAndPopulatesNotice() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo()
        val viewModel = createViewModel()
        val linkItemsChangedBefore = ServiceLocator.linkItemsChanged.value

        val frozenItem = LinkItemInfo(id = "1", label = "some item")
        coEvery { ServiceLocator.api.syncLimits(10) } returns SyncLimitsResult(
            trackableItemsLeft = 0,
            frozenItems = listOf(frozenItem),
            unfrozenItems = emptyList()
        )

        customerInfoFlow.value = FakeCustomerInfo(activeSubscriptions = setOf("sub_10_items"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(frozenItem), viewModel.syncLimitsNotice.value?.frozenItems)
        assertEquals(linkItemsChangedBefore + 1, ServiceLocator.linkItemsChanged.value)
    }

    @Test
    fun sameTierOnReEmitDoesNotTriggerLimitSync() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo()
        val viewModel = createViewModel()
        val linkItemsChangedBefore = ServiceLocator.linkItemsChanged.value

        customerInfoFlow.value = FakeCustomerInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { ServiceLocator.api.syncLimits(any()) }
        assertNull(viewModel.syncLimitsNotice.value)
        assertEquals(linkItemsChangedBefore, ServiceLocator.linkItemsChanged.value)
    }

    @Test
    fun syncLimitsResultWithNoFrozenOrUnfrozenItemsDoesNotSetNotice() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo()
        val viewModel = createViewModel()
        val linkItemsChangedBefore = ServiceLocator.linkItemsChanged.value

        coEvery { ServiceLocator.api.syncLimits(any()) } returns SyncLimitsResult(
            trackableItemsLeft = 5,
            frozenItems = emptyList(),
            unfrozenItems = emptyList()
        )

        customerInfoFlow.value = FakeCustomerInfo(activeSubscriptions = setOf("sub_10_items"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.syncLimitsNotice.value)
        assertEquals(linkItemsChangedBefore, ServiceLocator.linkItemsChanged.value)
    }

    @Test
    fun dismissSyncLimitsNoticeClearsTheNotice() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns FakeCustomerInfo()
        val viewModel = createViewModel()

        coEvery { ServiceLocator.api.syncLimits(10) } returns SyncLimitsResult(
            trackableItemsLeft = 0,
            frozenItems = listOf(LinkItemInfo(id = "1", label = "some item")),
            unfrozenItems = emptyList()
        )
        customerInfoFlow.value = FakeCustomerInfo(activeSubscriptions = setOf("sub_10_items"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.syncLimitsNotice.value != null)

        viewModel.dismissSyncLimitsNotice()

        assertNull(viewModel.syncLimitsNotice.value)
    }

    @Test
    fun restorePurchasesSuccessUpdatesState() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null
        val viewModel = createViewModel()

        coEvery { RevenueCatManager.restorePurchases() } returns FakeCustomerInfo(
            activeEntitlements = setOf("premium"),
            isPremium = true,
            activeSubscriptions = setOf("sub_5_items")
        )

        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.subscriptionState.value

        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionTier.BASE, state.tier)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun restorePurchasesReturningNullSetsError() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null
        val viewModel = createViewModel()

        coEvery { RevenueCatManager.restorePurchases() } returns null

        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("No purchases to restore", viewModel.subscriptionState.value.error)
    }

    @Test
    fun restorePurchasesThrowingSetsErrorToExceptionMessage() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null
        val viewModel = createViewModel()

        coEvery { RevenueCatManager.restorePurchases() } throws RuntimeException("restore failed")

        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("restore failed", viewModel.subscriptionState.value.error)
    }

    @Test
    fun settersUpdateStateDirectly() {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null
        val viewModel = createViewModel()

        viewModel.setLoading(true)
        assertTrue(viewModel.subscriptionState.value.isLoading)

        viewModel.setError("oops")
        assertEquals("oops", viewModel.subscriptionState.value.error)
        assertFalse(viewModel.subscriptionState.value.isLoading)

        viewModel.clearError()
        assertNull(viewModel.subscriptionState.value.error)
    }

    @Test
    fun hasEntitlementDelegatesToRevenueCatManager() = runTest(testDispatcher) {
        coEvery { RevenueCatManager.fetchCustomerInfo() } returns null
        val viewModel = createViewModel()

        coEvery { RevenueCatManager.hasActiveEntitlement("premium") } returns true

        assertTrue(viewModel.hasEntitlement("premium"))
    }
}
