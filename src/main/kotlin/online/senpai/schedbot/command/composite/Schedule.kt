package online.senpai.schedbot.command.composite

import discord4j.common.util.TimestampFormat
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.rest.util.ApplicationCommandOptionType
import discord4j.rest.util.Color
import online.senpai.newbase60.numberToSexagesimal
import online.senpai.newbase60.sexagesimalToNumber
import online.senpai.schedbot.command.CompositeClassDefinition
import online.senpai.schedbot.command.FIRST_TEST_GUILD_ID
import online.senpai.schedbot.command.SECOND_TEST_GUILD_ID
import online.senpai.schedbot.command.defineCompositeCommand
import online.senpai.schedbot.command.defineSubcommand
import online.senpai.schedbot.dto.DelayedTextChannelNotificationDto
import online.senpai.schedbot.extension.getSubcommandOptionAsChannel
import online.senpai.schedbot.extension.getSubcommandOptionAsLongOrElse
import online.senpai.schedbot.extension.getSubcommandOptionAsMention
import online.senpai.schedbot.extension.getSubcommandOptionAsStringOrElse
import online.senpai.schedbot.extension.parseAsOffsetDateTime
import online.senpai.schedbot.service.SchedulingService
import online.senpai.schedbot.util.TOF_HEH
import online.senpai.schedbot.util.TOF_HMM
import online.senpai.schedbot.util.TOF_MYOW
import online.senpai.schedbot.util.TOF_SLY
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.cast
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.kotlin.core.util.function.component4
import reactor.util.function.Tuples
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

object Schedule : CompositeSlashCommand {
    override val enabled = true
    override val guilds: Flux<Long> = Flux.just(FIRST_TEST_GUILD_ID, SECOND_TEST_GUILD_ID)
    override val definition: CompositeClassDefinition = defineCompositeCommand {
        name = "sched"
        description = "Scheduler"
        subcommands {
            +ScheduleAdd
            +ScheduleList
            +ScheduleRemove
        }
    }
}

object ScheduleRemove : CompositeSlashCommand.Subcommand, KoinComponent {
    private val schedulingService: SchedulingService by inject()

    override val definition: ApplicationCommandOptionData = defineSubcommand {
        name = "remove"
        description = "Removes an event"
        option {
            name = "id"
            description = "The id of an event"
            type = ApplicationCommandOptionType.STRING
            required = true
        }
    }

    override fun handler(event: SlashCommandEvent): Mono<Void> =
        Mono.just(event.getSubcommandOptionAsStringOrElse("id", ""))
            .flatMap { id: String ->
                schedulingService
                    .removeEvent(sexagesimalToNumber(id))
                    .map { removed: Boolean ->
                        Tuples.of(id, removed)
                    }
            }
            .flatMap { (id: String, removed: Boolean) ->
                if (removed) {
                    event.reply(
                        InteractionApplicationCommandCallbackSpec.create()
                            .withEmbeds(
                                EmbedCreateSpec.create()
                                    .withColor(Color.SUMMER_SKY)
                                    .withTitle("Id: $id")
                                    .withDescription("An event with the given id has been successfully removed")
                                    .withThumbnail(TOF_HMM)
                            )
                    )
                } else {
                    Mono.error(IllegalArgumentException("Internal error"))
                }
            }
            .onErrorResume { throwable: Throwable ->
                errorHandler(event, throwable)
            }
}

object ScheduleList : CompositeSlashCommand.Subcommand, KoinComponent {
    private val schedulingService: SchedulingService by inject()

    override val definition: ApplicationCommandOptionData = defineSubcommand {
        name = "list"
        description = "Displays all scheduled events in the queue"
    }

    override fun handler(event: SlashCommandEvent): Mono<Void> =
        event
            .reply(
                InteractionApplicationCommandCallbackSpec.create()
                    .withEmbeds(
                        EmbedCreateSpec.create()
                            .withColor(Color.SUMMER_SKY)
                            .withDescription("Future events:")
                            .withFields( // TODO only supports up to 25 fields
                                schedulingService
                                    .activeJobs
                                    .map { (key: Long, value: SchedulingService.ScheduledJob) ->
                                        EmbedCreateFields.Field.of(
                                            numberToSexagesimal(key),
                                            value.toString(),
                                            true
                                        )
                                    }
                            )
                            .withThumbnail(if (schedulingService.activeJobs.isNotEmpty()) TOF_SLY else "")
                            .withFooter(
                                EmbedCreateFields.Footer.of("Total: ${schedulingService.activeJobs.size}", null)
                            )
                    )
            )
            .onErrorResume { throwable: Throwable ->
                errorHandler(event, throwable)
            }
}

private const val UTC_MIN = -12
private const val UTC_MAX = 12
private const val LAST_MENTIONABLE = 9

object ScheduleAdd : CompositeSlashCommand.Subcommand, KoinComponent {
    private val schedulingService: SchedulingService by inject()

    override val definition: ApplicationCommandOptionData = defineSubcommand {
        name = "add"
        description = "Adds a new event to the scheduler"
        option {
            name = "date_time"
            description = "Specify date and time in one of the formats: " +
                    "[10:30] [22 10:30]  [08-22 10:30] [2021-08-22 10:30]"
            type = ApplicationCommandOptionType.STRING
            required = true
        }
        option {
            name = "utc_offset"
            description = "Specify the UTC offset"
            type = ApplicationCommandOptionType.INTEGER
            required = true
            choices {
                val pattern = "UTC%+d"
                for (i: Int in UTC_MAX downTo UTC_MIN) {
                    pattern.format(i) to i
                }
            }
        }
        option {
            name = "message"
            description = "The message to be posted"
            type = ApplicationCommandOptionType.STRING
            required = true
        }
        option {
            name = "mentionable_0"
            description = "Specify the role or user to be notified"
            type = ApplicationCommandOptionType.MENTIONABLE
            required = true
        }
        for (i: Int in 1..LAST_MENTIONABLE) {
            option {
                name = "mentionable_$i"
                description = "Specify the role or user to be notified"
                type = ApplicationCommandOptionType.MENTIONABLE
                required = false
            }
        }
        option {
            name = "channel"
            description = "Specify the channel in which the event will be triggered"
            type = ApplicationCommandOptionType.CHANNEL
            required = false
        }
    }

    override fun handler(event: SlashCommandEvent): Mono<Void> =
        Mono.zip(
            event.getSubcommandOptionAsStringOrElse("date_time", "")
                .parseAsOffsetDateTime()
                .map { datetime: LocalDateTime ->
                    datetime.atOffset(
                        ZoneOffset.ofHours(
                            event.getSubcommandOptionAsLongOrElse("utc_offset", 0).toInt()
                        )
                    )
                },
            Mono.just(event.getSubcommandOptionAsStringOrElse("message", "")),
            Mono.zip(
                (0..LAST_MENTIONABLE).map { i: Int ->
                    event.getSubcommandOptionAsMention("mentionable_$i", "")
                }
            ) { mentions: Array<Any> ->
                mentions.filter { (it as String).isNotBlank() }.joinToString(separator = "")
            },
            event.getSubcommandOptionAsChannel("channel", event.interaction.channel.cast())
        )
            .flatMap { (datetime: OffsetDateTime, message: String, joinedMentions: String, channel: Channel) ->
                when {
                    datetime.isBefore(OffsetDateTime.now()) -> Mono.error( // TODO
                        IllegalArgumentException(
                            "You cannot schedule an event in a time that has already passed. " +
                                    "The specified date: $datetime"
                        )
                    )
                    channel.type != Channel.Type.GUILD_TEXT -> Mono.error( // TODO
                        IllegalArgumentException(
                            "The channel must be a text channel. The specified channel has type: ${channel.type}"
                        )
                    )
                    else -> Mono.just(
                        DelayedTextChannelNotificationDto(
                            id = event.interaction.id.asLong(),
                            createdOn = OffsetDateTime.now(),
                            fireOn = datetime,
                            channelId = channel.id,
                            authorId = event.interaction.user.id,
                            payload = "$message $joinedMentions"
                        )
                    )
                }
            }
            .map { jobData: DelayedTextChannelNotificationDto ->
                Tuples.of(schedulingService.scheduleNewEvent(jobData), jobData.fireOn, jobData.id)
            }
            .flatMap { (emitResult: Sinks.EmitResult, fireOn: OffsetDateTime, id: Long) ->
                emitResult.orThrow()
                event.reply(
                    InteractionApplicationCommandCallbackSpec.create()
                        .withEmbeds(
                            EmbedCreateSpec.create()
                                .withColor(Color.SUMMER_SKY)
                                .withTitle("Id: ${numberToSexagesimal(id)}")
                                .withDescription(
                                    "A notification will be sent " +
                                            TimestampFormat.RELATIVE_TIME.format(fireOn.toInstant())
                                )
                                .withTimestamp(fireOn.toInstant())
                                .withThumbnail(TOF_HEH)
                        )
                )
            }
            .onErrorResume { throwable: Throwable ->
                errorHandler(event, throwable)
            }
            .then()
}

fun errorHandler(event: SlashCommandEvent, throwable: Throwable): Mono<Void> =
    event.reply(
        InteractionApplicationCommandCallbackSpec.create()
            .withEmbeds(
                EmbedCreateSpec.create()
                    .withColor(Color.RED)
                    .withTitle("Houston, uhh, we have a problem!")
                    .withDescription(throwable.toString())
                    .withThumbnail(TOF_MYOW)
            )
    )
