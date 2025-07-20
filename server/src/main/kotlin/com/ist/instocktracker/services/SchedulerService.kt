package com.ist.instocktracker.services

import com.google.cloud.ServiceOptions
import com.google.cloud.scheduler.v1.CloudSchedulerClient
import com.google.cloud.scheduler.v1.CreateJobRequest
import com.google.cloud.scheduler.v1.GetJobRequest
import com.google.cloud.scheduler.v1.HttpTarget
import com.google.cloud.scheduler.v1.Job
import com.google.cloud.scheduler.v1.JobName
import com.google.cloud.scheduler.v1.LocationName
import com.google.cloud.scheduler.v1.HttpMethod
import com.google.cloud.Timestamp
import com.google.protobuf.ByteString
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import java.time.Instant
import java.util.UUID

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
                .setUri("$serverBaseUrl/api/v1/link-item/${linkItem.id}/check")
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