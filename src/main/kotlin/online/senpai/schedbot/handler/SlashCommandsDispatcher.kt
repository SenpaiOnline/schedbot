package online.senpai.schedbot.handler

import discord4j.core.event.domain.InteractionCreateEvent
import reactor.core.publisher.Mono

interface SlashCommandsDispatcher {
    fun initialize(): Mono<Void>
    fun destroy(): Mono<Void>
    fun dispatchInteractionEvent(event: InteractionCreateEvent): Mono<Void>
}
