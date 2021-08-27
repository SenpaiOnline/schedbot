package online.senpai.schedbot.service

import discord4j.common.util.TimestampFormat
import online.senpai.schedbot.dto.DelayedTextChannelNotificationDto
import org.reactivestreams.Subscription
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

interface SchedulingService {
    val activeJobs: Set<Map.Entry<Long, ScheduledJob>>
    fun initialize(): Mono<Void>
    fun scheduleNewEvent(jobData: DelayedTextChannelNotificationDto): Sinks.EmitResult
    fun removeEvent(jobId: Long): Mono<Boolean>

    data class ScheduledJob(val subscription: Subscription, val data: DelayedTextChannelNotificationDto) {
        override fun toString(): String =
            """
                Created on ${TimestampFormat.DEFAULT.format(data.createdOn.toInstant())}
                Be Fired on ${TimestampFormat.DEFAULT.format(data.fireOn.toInstant())}
                Payload: ${data.payload}
            """.trimIndent()
    }
}
