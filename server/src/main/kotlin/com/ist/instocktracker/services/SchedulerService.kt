package com.ist.instocktracker.services

import com.google.cloud.ServiceOptions
import com.google.cloud.scheduler.v1.*
import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.utils.parseStartAt
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.*

/**
 * Service class to manage Cloud Scheduler jobs for LinkItems
 */
class SchedulerService(
    private val projectId: String = ServiceOptions.getDefaultProjectId(),
    private val location: String,
    private val serverBaseUrl: String
) {
    /**
     * Creates a new schedule job for a LinkItem
     * @param linkItem The LinkItem to create a schedule for
     * @return The ID of the created schedule job
     */
    fun createSchedule(linkItem: LinkItem): String {
        CloudSchedulerClient.create().use { client ->
            val parent = LocationName.of(projectId, location)

            // Generate a unique job ID
            val jobId = "link-item-check-${UUID.randomUUID()}"
            val jobName = JobName.of(projectId, location, jobId).toString()

            // Create HTTP target for the job
            val httpTarget = HttpTarget.newBuilder()
                .setUri("$serverBaseUrl/api/v1/link-items/${linkItem.id}/check")
                .setHttpMethod(HttpMethod.POST)
                .setBody(ByteString.copyFromUtf8("{}"))
                .putHeaders("Content-Type", "application/json")
                .build()

            // Set schedule based on LinkItem interval
            val schedule = buildScheduleExpression(linkItem.interval)

            // Build the job
            val job = Job.newBuilder()
                .setName(jobName)
                .setHttpTarget(httpTarget)
                .setSchedule(schedule)
                .build()

            // Create the job request
            val request = CreateJobRequest.newBuilder()
                .setParent(parent.toString())
                .setJob(job)
                .build()

            // Create the job
            client.createJob(request)

            return jobId
        }
    }

    /**
     * Gets a schedule job by ID
     * @param jobId The ID of the schedule job to get
     * @return The Job object if found, null otherwise
     */
    fun getSchedule(jobId: String): Job? {
        CloudSchedulerClient.create().use { client ->
            val jobName = JobName.of(projectId, location, jobId)

            val request = GetJobRequest.newBuilder()
                .setName(jobName.toString())
                .build()

            return try {
                client.getJob(request)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Pauses a schedule job
     * @param jobId The ID of the schedule job to pause
     * @return true if successful, false otherwise
     */
    fun pauseJob(jobId: String): Boolean {
        CloudSchedulerClient.create().use { client ->
            val jobName = JobName.of(projectId, location, jobId)
            return try {
                client.pauseJob(jobName)
                true
            } catch (e: Exception) {
                println("Error pausing job $jobId: ${e.message}")
                false
            }
        }
    }

    /**
     * Resumes a paused schedule job
     * @param jobId The ID of the schedule job to resume
     * @return true if successful, false otherwise
     */
    fun resumeJob(jobId: String): Boolean {
        CloudSchedulerClient.create().use { client ->
            val jobName = JobName.of(projectId, location, jobId)
            return try {
                client.resumeJob(jobName)
                true
            } catch (e: Exception) {
                println("Error resuming job $jobId: ${e.message}")
                false
            }
        }
    }

    /**
     * Sets the schedule time (next run) for a scheduled job based on the startAt date string.
     * Parses the date string and checks if it has time or not.
     * If there is no time (hour and minute are both 0), sets it to 00:00 of the day.
     *
     * @param jobId The ID of the schedule job to update
     * @param startAt The date string in ISO format (e.g., "2024-12-04T10:30" or "2024-12-04T00:00")
     * @return true if successful, false otherwise
     */
    fun setScheduleTime(jobId: String, startAt: String): Boolean {
        CloudSchedulerClient.create().use { client ->
            val jobName = JobName.of(projectId, location, jobId)

            return try {
                // Parse the startAt date string
                val dateTime = parseStartAt(startAt)

                // Convert to epoch seconds for the Timestamp
                val epochSeconds = dateTime.toInstant(TimeZone.UTC).epochSeconds

                // Create a protobuf Timestamp for the schedule time
                val scheduleTime = Timestamp.newBuilder()
                    .setSeconds(epochSeconds)
                    .build()

                // Get the existing job to preserve its configuration
                val existingJob = client.getJob(jobName)

                // Update the job with the new schedule time
                val updatedJob = existingJob.toBuilder()
                    .setScheduleTime(scheduleTime)
                    .build()

                val updateRequest = UpdateJobRequest.newBuilder()
                    .setJob(updatedJob)
                    .build()

                client.updateJob(updateRequest)
                true
            } catch (e: Exception) {
                println("Error setting schedule time for job $jobId: ${e.message}")
                false
            }
        }
    }

    /**
     * Builds a schedule expression based on the LinkItem interval
     * @param interval The interval to build the schedule expression for
     * @return The schedule expression
     */
    private fun buildScheduleExpression(interval: Interval): String {
        // Convert the interval to a cron expression
        return when (interval.duration) {
            DurationUnit.MINUTES -> "*/${interval.unit} * * * *" // Every n minutes
            DurationUnit.HOURS -> "0 */${interval.unit} * * *" // At minute 0, every n hours
            DurationUnit.DAYS -> "0 0 */${interval.unit} * *" // At 00:00, every n days
        }
    }
}