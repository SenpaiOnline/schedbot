package online.senpai.schedbot.handler

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono

class EventHandlerImpl : EventHandler, KoinComponent {
    private val discordClient: GatewayDiscordClient by inject()
    private val slashCommandsDispatcher: SlashCommandsDispatcher by inject()
    private val eventAdapter: ReactiveEventAdapter = object : ReactiveEventAdapter() {
        override fun onSlashCommand(event: SlashCommandEvent): Mono<Void> =
            slashCommandsDispatcher.dispatchSlashCommandEvent(event)
    }

    override fun initialize(): Mono<Void> = discordClient.on(eventAdapter).then()

    override fun awaitTermination(): Mono<Void> = discordClient.onDisconnect()
}
