package online.senpai.schedbot.handler

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandData
import mu.KLogger
import mu.KotlinLogging
import online.senpai.schedbot.command.SlashCommand
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger {}

typealias SlashCommandHandler = (event: InteractionCreateEvent) -> Mono<Void>

class SlashCommandsDispatcherImpl : SlashCommandsDispatcher, KoinComponent {
    private val discordClient: GatewayDiscordClient by inject()
    private val slashCommandsRegistry: MutableMap<String, SlashCommandHandler> = mutableMapOf()
    private val affectedGuilds: MutableSet<Long> = mutableSetOf()
    private val slashCommandImplementations: Flux<out SlashCommand> = Flux.create { sink: FluxSink<SlashCommand> ->
        SlashCommand::class.sealedSubclasses.forEach { subclass: KClass<out SlashCommand> ->
            subclass.objectInstance?.let { sink.next(it) }
        }
    }

    override fun initialize(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flux()
            .flatMap { applicationId: Long ->
                slashCommandImplementations // FIXME commands can overwrite each other
                    .filter { command: SlashCommand -> command.enabled }
                    .flatMap { command: SlashCommand ->
                        when (val scope: SlashCommand.Scope = command.scope) {
                            is SlashCommand.Scope.Guild -> {
                                discordClient
                                    .restClient
                                    .applicationService
                                    .createGuildApplicationCommand(
                                        applicationId,
                                        scope.guildId,
                                        command.commandRequest
                                    )
                                    .doOnSuccess { data: ApplicationCommandData? ->
                                        logger.debug {
                                            "Registration of the command ${data?.name()} for the guild with id" +
                                                    " ${scope.guildId} resulted in $data"
                                        }
                                        if (data != null) {
                                            affectedGuilds.add(scope.guildId)
                                            slashCommandsRegistry[data.name()] = command::handler
                                        }
                                    }
                                    .doOnError { throwable: Throwable ->
                                        logger.error(throwable) {
                                            "Couldn't create the command ${command.commandRequest.name()}" +
                                                    "in the guild with id ${scope.guildId}"
                                        }
                                    }
                            }
                            is SlashCommand.Scope.Global -> {
                                discordClient
                                    .restClient
                                    .applicationService
                                    .createGlobalApplicationCommand(
                                        applicationId,
                                        command.commandRequest
                                    )
                                    .doOnSuccess { data: ApplicationCommandData? ->
                                        logger.debug { "Registration of the command ${data?.name()} resulted in $data" }
                                        if (data != null) {
                                            slashCommandsRegistry[data.name()] = command::handler
                                        }
                                    }
                                    .doOnError { throwable: Throwable ->
                                        logger.error(throwable) {
                                            "Couldn't create the command ${command.commandRequest.name()}"
                                        }
                                    }
                            }
                        }
                    }
                    .then()
            }
            .then()

    override fun destroy(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flux()
            .flatMap { applicationId: Long ->
                Flux
                    .fromIterable(affectedGuilds)
                    .flatMap { guildId: Long ->
                        discordClient
                            .restClient
                            .applicationService
                            .getGuildApplicationCommands(applicationId, guildId)
                            .flatMap { applicationData: ApplicationCommandData ->
                                discordClient
                                    .restClient
                                    .applicationService
                                    .deleteGuildApplicationCommand(
                                        applicationId,
                                        guildId,
                                        applicationData.id().toLong() // TODO ask the core devs about it
                                    )
                                    .doOnSuccess {
                                        logger.debug {
                                            "The command ${applicationData.name()} from the" +
                                                    " guild with id $guildId has been successfully removed"
                                        }
                                    }
                                    .doOnError { throwable: Throwable ->
                                        logger.error(throwable) {
                                            "Couldn't delete the command ${applicationData.name()} with id" +
                                                    " ${applicationData.id()} from the guild with id $guildId"
                                        }
                                    }
                            }
                    }
                    .thenEmpty(
                        discordClient
                            .restClient
                            .applicationService
                            .getGlobalApplicationCommands(applicationId)
                            .flatMap { applicationData: ApplicationCommandData ->
                                discordClient
                                    .restClient
                                    .applicationService
                                    .deleteGlobalApplicationCommand(applicationId, applicationData.id().toLong())
                                    .doOnSuccess {
                                        logger.debug {
                                            "The command ${applicationData.name()} has been successfully removed"
                                        }
                                    }
                                    .doOnError { throwable: Throwable ->
                                        logger.error(throwable) {
                                            "Couldn't delete command the ${applicationData.name()}" +
                                                    " with id ${applicationData.id()}"
                                        }
                                    }
                            }
                    )
            }
            .then()

    override fun dispatchInteractionEvent(event: InteractionCreateEvent): Mono<Void> {
        return slashCommandsRegistry[event.commandName]?.invoke(event)
            ?: event.reply("I can't handle that command right now :(")
    }
}
