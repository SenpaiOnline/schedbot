package online.senpai.schedbot.command

import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import reactor.core.publisher.Mono

sealed interface SlashCommand {
    val commandRequest: ApplicationCommandRequest
    val scope: Scope
    fun handler(event: InteractionCreateEvent): Mono<Void>

    sealed interface Scope {
        data class Guild(val guildId: Long) : Scope
        object Global : Scope
    }
}
