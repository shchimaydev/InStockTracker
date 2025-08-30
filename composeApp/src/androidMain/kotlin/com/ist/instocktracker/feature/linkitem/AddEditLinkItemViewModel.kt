package com.ist.instocktracker.feature.linkitem

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditLinkItemViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    // Form state
    var link by mutableStateOf("")
    var label by mutableStateOf("")
    var mode by mutableStateOf(Mode.IN_STOCK)
    
    // Date/Time state - separate fields for UI
    var selectedDate by mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }.time)
    var selectedTime by mutableStateOf(Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.time)
    var addPreciseTime by mutableStateOf(false)
    
    // Combined startAt for API compatibility
    var startAt by mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }.time)
        private set
    
    var intervalUnit by mutableStateOf(1)
    var intervalDuration by mutableStateOf(DurationUnit.HOURS)
    var isActive by mutableStateOf(true)
    var additionalInstructions by mutableStateOf("")
    
    // UI state
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isEditMode by mutableStateOf(false)
    var linkItemId by mutableStateOf<String?>(null)
    
    // Validation
    var linkError by mutableStateOf<String?>(null)
    var intervalError by mutableStateOf<String?>(null)

    fun initialize(linkItemId: String?) {
        this.linkItemId = linkItemId
        isEditMode = linkItemId != null
        
        if (linkItemId != null) {
            fetchLinkItem(linkItemId)
        }
    }

    private fun fetchLinkItem(id: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val linkItem = ServiceLocator.api.getLinkItem(id)
                populateForm(linkItem)
            } catch (e: Exception) {
                errorMessage = "Failed to load link item: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun populateForm(linkItem: LinkItem) {
        link = linkItem.link
        label = linkItem.label ?: ""
        mode = linkItem.mode
        isActive = linkItem.isActive
        additionalInstructions = linkItem.additionalInstructions ?: ""
        intervalUnit = linkItem.interval.unit
        intervalDuration = linkItem.interval.duration
        
        // Parse startAt if available and split into date/time components
        linkItem.startAt?.let { startAtStr ->
            try {
                val parsedDate = dateFormat.parse(startAtStr)
                if (parsedDate != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = parsedDate
                    
                    // Set selectedDate (date part only)
                    selectedDate = parsedDate
                    
                    // Check if time is not 00:00 to determine if precise time was set
                    val hasTime = calendar.get(Calendar.HOUR_OF_DAY) != 0 || calendar.get(Calendar.MINUTE) != 0
                    addPreciseTime = hasTime
                    
                    if (hasTime) {
                        // Set selectedTime (time part only)
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                        timeCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        timeCalendar.set(Calendar.SECOND, 0)
                        timeCalendar.set(Calendar.MILLISECOND, 0)
                        selectedTime = timeCalendar.time
                    }
                    
                    // Update combined startAt field
                    updateStartAt()
                }
            } catch (e: Exception) {
                // Keep default values if parsing fails
            }
        }
    }

    fun onLinkChanged(newLink: String) {
        link = newLink
        linkError = null
        
        // Auto-populate label if empty and link is valid
        if (label.isEmpty() && newLink.isNotEmpty()) {
            try {
                val uri = Uri.parse(newLink)
                uri.host?.let { host ->
                    label = host
                }
            } catch (e: Exception) {
                // Ignore parsing errors for label auto-population
            }
        }
    }

    fun onLabelChanged(newLabel: String) {
        label = newLabel
    }

    fun onModeChanged(newMode: Mode) {
        mode = newMode
    }

    // Helper method to calculate combined startAt from separate date/time
    private fun updateStartAt() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        if (addPreciseTime) {
            val timeCalendar = Calendar.getInstance()
            timeCalendar.time = selectedTime
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        } else {
            // Set to 00:00 if precise time is not selected
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        startAt = calendar.time
    }

    fun onDateChanged(newDate: Date) {
        selectedDate = newDate
        updateStartAt()
    }

    fun onTimeChanged(newTime: Date) {
        selectedTime = newTime
        updateStartAt()
    }

    fun onAddPreciseTimeChanged(addTime: Boolean) {
        addPreciseTime = addTime
        updateStartAt()
    }

    @Deprecated("Use onDateChanged and onTimeChanged instead")
    fun onStartAtChanged(newStartAt: Date) {
        startAt = newStartAt
    }

    fun onIntervalUnitChanged(newUnit: Int) {
        intervalUnit = newUnit
        validateInterval()
    }

    fun onIntervalDurationChanged(newDuration: DurationUnit) {
        intervalDuration = newDuration
        // Reset unit to valid range when duration changes
        intervalUnit = when (newDuration) {
            DurationUnit.MINUTES -> intervalUnit.coerceIn(15, 60)
            DurationUnit.HOURS -> intervalUnit.coerceIn(1, 23)
            DurationUnit.DAYS -> intervalUnit.coerceIn(1, 30)
        }
        validateInterval()
    }

    fun onIsActiveChanged(newIsActive: Boolean) {
        isActive = newIsActive
    }

    fun onAdditionalInstructionsChanged(newInstructions: String) {
        additionalInstructions = newInstructions
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate link
        if (link.isEmpty()) {
            linkError = "Link is required"
            isValid = false
        } else {
            linkError = null
        }

        // Validate interval
        if (!validateInterval()) {
            isValid = false
        }

        return isValid
    }

    private fun validateInterval(): Boolean {
        val isValid = when (intervalDuration) {
            DurationUnit.MINUTES -> intervalUnit in 15..60
            DurationUnit.HOURS -> intervalUnit in 1..23
            DurationUnit.DAYS -> intervalUnit in 1..30
        }

        intervalError = if (!isValid) {
            when (intervalDuration) {
                DurationUnit.MINUTES -> "Minutes must be between 15 and 60"
                DurationUnit.HOURS -> "Hours must be between 1 and 23"
                DurationUnit.DAYS -> "Days must be between 1 and 30"
            }
        } else {
            null
        }

        return isValid
    }

    fun saveItem(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val linkItem = LinkItem(
                    id = linkItemId ?: "",
                    link = link,
                    label = label.ifEmpty { null },
                    mode = mode,
                    startAt = dateFormat.format(startAt),
                    additionalInstructions = additionalInstructions.ifEmpty { null },
                    isActive = isActive,
                    interval = Interval(intervalUnit, intervalDuration)
                )

                if (isEditMode && linkItemId != null) {
                    ServiceLocator.api.updateLinkItem(linkItemId!!, linkItem)
                } else {
                    ServiceLocator.api.createLinkItem(linkItem)
                }

                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to save link item: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}