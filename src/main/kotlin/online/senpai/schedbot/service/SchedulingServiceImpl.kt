package online.senpai.schedbot.service

import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.presence.ClientActivity.competing
import discord4j.core.`object`.presence.ClientPresence.online
import mu.KLogger
import mu.KotlinLogging
import online.senpai.schedbot.dto.DelayedTextChannelNotificationDto
import online.senpai.schedbot.entity.ScheduledEvent
import online.senpai.schedbot.mapper.ScheduledMessagesMapper
import online.senpai.schedbot.repository.ScheduledRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.reactivestreams.Subscription
import reactor.bool.BooleanUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.cast
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap

private val logger: KLogger = KotlinLogging.logger {}

private const val INITIAL_JOBS_CAPACITY = 25

class SchedulingServiceImpl : SchedulingService, KoinComponent {
    private val discordClient: GatewayDiscordClient by inject()
    private val scheduledRepository: ScheduledRepository by inject()
    private val messagesMapper: ScheduledMessagesMapper by inject()
    private val sink: Sinks.Many<DelayedTextChannelNotificationDto> = Sinks.many().multicast().onBackpressureBuffer()
    private val upstream: Flux<DelayedTextChannelNotificationDto> = sink.asFlux()
    private val _activeJobs = ConcurrentHashMap<Long, SchedulingService.ScheduledJob>(INITIAL_JOBS_CAPACITY)
    override val activeJobs: Set<Map.Entry<Long, SchedulingService.ScheduledJob>> = _activeJobs.entries

    init {
        upstream
            .flatMap { data: DelayedTextChannelNotificationDto ->
                Mono.defer {
                    discordClient
                        .getChannelById(data.channelId)
                        .cast<MessageChannel>()
                        .map { messageChannel: MessageChannel ->
                            messageChannel.createMessage(data.payload)
                        }
                        .delayElement(Duration.between(OffsetDateTime.now(), data.fireOn))
                        .doOnSubscribe { subscription: Subscription -> // FIXME
                            _activeJobs[data.id] = SchedulingService.ScheduledJob(subscription, data)
                            updateStatus().subscribe()
                        }
                        .doFinally {
                            _activeJobs.remove(data.id)
                            updateStatus().subscribe()
                        }
                }
            }
            .flatMap { it }
            .doOnNext { message: Message ->
                logger.debug { "Sending a message with content: $message" }
            }
            .subscribe()

        upstream
            .filterWhen { data: DelayedTextChannelNotificationDto ->
                BooleanUtils.not(scheduledRepository.existsById(data.id))
            }
            .flatMap { data: DelayedTextChannelNotificationDto ->
                scheduledRepository.save(messagesMapper.transformToEntity(data))
            }
            .subscribe()
    }

    private fun updateStatus(): Mono<Void> =
        discordClient
            .updatePresence(online(competing("Future events: ${_activeJobs.size}"))) // TODO

    override fun initialize(): Mono<Void> =
        scheduledRepository
            .findAll()
            .filter { event: ScheduledEvent ->
                event.fireOn.isAfter(OffsetDateTime.now())
            }
            .map { entity: ScheduledEvent ->
                scheduleNewEvent(messagesMapper.transformToDto(entity))
            }
            .doOnNext { result: Sinks.EmitResult ->
                result.orThrow()
            }
            .then()

    override fun scheduleNewEvent(jobData: DelayedTextChannelNotificationDto): Sinks.EmitResult =
        sink.tryEmitNext(jobData)

    override fun removeEvent(jobId: Long): Mono<Boolean> =
        runCatching {
            _activeJobs[jobId]!!.subscription.cancel()
            _activeJobs.remove(jobId)
            updateStatus().subscribe()
        }
            .fold(
                { scheduledRepository.deleteById(jobId) },
                { Mono.error(IllegalArgumentException("Couldn't find a job with the given id")) }
            )
}

