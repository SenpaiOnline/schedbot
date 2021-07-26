package online.senpai.schedbot.handler

import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandData
import discord4j.rest.util.ApplicationCommandOptionType
import mu.KLogger
import mu.KotlinLogging
import online.senpai.schedbot.Trie
import online.senpai.schedbot.command.composite.CompositeSlashCommand
import online.senpai.schedbot.command.standalone.StandaloneSlashCommand
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.Logger
import reactor.util.Loggers
import reactor.util.function.Tuples
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger {}
private val reactorLogger: Logger = Loggers.getLogger(SlashCommandsDispatcherImpl::class.java)

typealias SlashCommandHandler = (event: SlashCommandEvent) -> Mono<Void>

class SlashCommandsDispatcherImpl : SlashCommandsDispatcher, KoinComponent {
    private val discordClient: GatewayDiscordClient by inject()
    private val slashCommandsRegistry = Trie<String, SlashCommandHandler>()
    private val affectedGuilds: MutableSet<Long> = mutableSetOf()
    private val compositeCommandImplementations: Flux<CompositeSlashCommand> =
        Flux.create { sink: FluxSink<CompositeSlashCommand> ->
            CompositeSlashCommand::class.sealedSubclasses.forEach { subclass: KClass<out CompositeSlashCommand> ->
                subclass.objectInstance?.let(sink::next)
            }
            sink.complete()
        }
    private val standaloneCommandImplementations: Flux<StandaloneSlashCommand> =
        Flux.create { sink: FluxSink<StandaloneSlashCommand> ->
            StandaloneSlashCommand::class.sealedSubclasses.forEach { subclass: KClass<out StandaloneSlashCommand> ->
                subclass.objectInstance?.let(sink::next)
            }
            sink.complete()
        }

    private fun createStandaloneCommands(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flatMapMany { appId: Long ->
                standaloneCommandImplementations
                    .filter { command: StandaloneSlashCommand -> command.enabled }
                    .map { command: StandaloneSlashCommand -> Tuples.of(appId, command) }
            }
            .flatMap { (appId: Long, command: StandaloneSlashCommand) ->
                command
                    .guilds
                    .defaultIfEmpty(-1L) // TODO better way?
                    .map { guildId: Long -> Tuples.of(appId, command, guildId) }
            }
            .flatMap { (appId: Long, command: StandaloneSlashCommand, guildId: Long) ->
                if (guildId > 0) {
                    createStandaloneGuildCommand(appId, guildId, command)
                } else {
                    createStandaloneGlobalCommand(appId, command)
                }
            }
            .log(reactorLogger)
            .then()

    private fun createStandaloneGuildCommand(
        appId: Long,
        guildId: Long,
        command: StandaloneSlashCommand
    ): Mono<ApplicationCommandData> =
        discordClient
            .restClient
            .applicationService
            .createGuildApplicationCommand(
                appId,
                guildId,
                command.definition
            )
            .doOnSuccess { data: ApplicationCommandData? ->
                data?.let { commandData: ApplicationCommandData ->
                    slashCommandsRegistry.put(
                        commandData.name(),
                        guildId.toString(),
                        payload = command::handler
                    )
                    affectedGuilds.add(guildId)
                }
            }
            .doOnError { throwable: Throwable ->
                logger.error(throwable) {
                    "Couldn't create the command ${command.definition.name()} for guild $guildId"
                }
            }

    private fun createStandaloneGlobalCommand(
        appId: Long,
        command: StandaloneSlashCommand
    ): Mono<ApplicationCommandData> =
        discordClient
            .restClient
            .applicationService
            .createGlobalApplicationCommand(
                appId,
                command.definition
            )
            .doOnSuccess { data: ApplicationCommandData? ->
                data?.let { commandData: ApplicationCommandData ->
                    slashCommandsRegistry.put(
                        commandData.name(),
                        payload = command::handler
                    )
                }
            }

    private fun createCompositeCommands(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flatMapMany { appId: Long ->
                compositeCommandImplementations
                    .filter { command: CompositeSlashCommand -> command.enabled }
                    .map { command: CompositeSlashCommand -> Tuples.of(appId, command) }
            }
            .flatMap { (appId: Long, command: CompositeSlashCommand) ->
                command
                    .guilds
                    .defaultIfEmpty(-1L) // TODO
                    .map { guildId: Long -> Tuples.of(appId, command, guildId) }
            }
            .flatMap { (appId: Long, command: CompositeSlashCommand, guildId: Long) ->
                if (guildId > 0) {
                    createCompositeGuildCommand(appId, guildId, command)
                } else {
                    createCompositeGlobalCommand(appId, command)
                }
            }
            .log(reactorLogger)
            .then()

    private fun createCompositeGuildCommand(
        appId: Long,
        guildId: Long,
        command: CompositeSlashCommand
    ): Mono<ApplicationCommandData> =
        discordClient
            .restClient
            .applicationService
            .createGuildApplicationCommand(
                appId,
                guildId,
                command.definition.slashApiDefinition
            )
            .doOnSuccess { data: ApplicationCommandData? ->
                data?.let { commandData: ApplicationCommandData ->
                    command
                        .definition
                        .subcommands
                        .forEach { subcommand: CompositeSlashCommand.Subcommand ->
                            slashCommandsRegistry.put(
                                commandData.name(),
                                subcommand.definition.name(),
                                guildId.toString(),
                                payload = subcommand::handler
                            )
                        }
                }
            }
            .doOnError { throwable: Throwable ->
                logger.error(throwable) {
                    "Couldn't create the command ${command.definition.slashApiDefinition.name()} for guild $guildId"
                }
            }

    private fun createCompositeGlobalCommand(
        appId: Long,
        command: CompositeSlashCommand
    ): Mono<ApplicationCommandData> =
        discordClient
            .restClient
            .applicationService
            .createGlobalApplicationCommand(
                appId,
                command.definition.slashApiDefinition
            )
            .doOnSuccess { data: ApplicationCommandData? ->
                data?.let { commandData: ApplicationCommandData ->
                    command
                        .definition
                        .subcommands
                        .forEach { subcommand: CompositeSlashCommand.Subcommand ->
                            slashCommandsRegistry.put(
                                commandData.name(),
                                subcommand.definition.name(),
                                payload = subcommand::handler
                            )
                        }
                }
            }

    private fun deleteGuildCommands(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flatMapMany { appId: Long ->
                Flux
                    .fromIterable(affectedGuilds)
                    .map { guildId: Long -> Tuples.of(appId, guildId) }
            }
            .flatMap { (appId: Long, guildId: Long) ->
                discordClient
                    .restClient
                    .applicationService
                    .getGuildApplicationCommands(appId, guildId)
                    .map { command: ApplicationCommandData -> Tuples.of(appId, guildId, command) }
            }
            .flatMap { (appId: Long, guildId: Long, command: ApplicationCommandData) ->
                discordClient
                    .restClient
                    .applicationService
                    .deleteGuildApplicationCommand(appId, guildId, command.id().toLong())
                    .doOnSuccess {
                        logger.debug {
                            "The command ${command.name()} has been successfully removed from guild $guildId"
                        }
                    }
                    .doOnError { throwable: Throwable ->
                        logger.error(throwable) {
                            "Couldn't delete command the ${command.name()} with id ${command.id()} from guild $guildId"
                        }
                    }
            }
            .log(reactorLogger)
            .then()

    private fun deleteGlobalCommands(): Mono<Void> =
        discordClient
            .restClient
            .applicationId
            .flatMapMany { appId: Long ->
                discordClient
                    .restClient
                    .applicationService
                    .getGlobalApplicationCommands(appId)
                    .map { command: ApplicationCommandData -> Tuples.of(appId, command) }
            }
            .flatMap { (appId: Long, command: ApplicationCommandData) ->
                discordClient
                    .restClient
                    .applicationService
                    .deleteGlobalApplicationCommand(appId, command.id().toLong())
                    .doOnSuccess {
                        logger.debug {
                            "The command ${command.name()} has been successfully removed"
                        }
                    }
                    .doOnError { throwable: Throwable ->
                        logger.error(throwable) {
                            "Couldn't delete command the ${command.name()} with id ${command.id()}"
                        }
                    }
            }
            .log(reactorLogger)
            .then()

    override fun initialize(): Mono<Void> =
        createStandaloneCommands()
            .then(createCompositeCommands())

    override fun destroy(): Mono<Void> =
        deleteGuildCommands()
            .then(deleteGlobalCommands())

    override fun dispatchSlashCommandEvent(event: SlashCommandEvent): Mono<Void> { // TODO better dispatching
        val route: MutableList<String> = mutableListOf(event.interaction.data.data().get().name().get())
        val subcommand: ApplicationCommandInteractionOption? = event
            .interaction
            .commandInteraction
            .get()
            .options
            .firstOrNull()
        val guildId: Long? = event // FIXME A global command sent from a guild won't be resolved
            .interaction
            .guildId
            .orElse(null)
            ?.asLong()
        if (subcommand != null && subcommand.type == ApplicationCommandOptionType.SUB_COMMAND) {
            route += subcommand.name
        }
        if (guildId != null) {
            route += guildId.toString()
        }
        logger.debug { "Dispatching event with a route: $route" }
        return slashCommandsRegistry.get(route)?.invoke(event)
            ?: event.reply("Unable to process the command <a:this_is_fine:860917939696173087>")
    }
}
