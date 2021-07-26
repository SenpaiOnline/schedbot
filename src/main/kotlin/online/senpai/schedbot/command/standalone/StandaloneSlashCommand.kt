package online.senpai.schedbot.command.standalone

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

sealed interface StandaloneSlashCommand {
    val enabled: Boolean
    val guilds: Flux<Long>
    val definition: ApplicationCommandRequest
    fun handler(event: SlashCommandEvent): Mono<Void>
}
