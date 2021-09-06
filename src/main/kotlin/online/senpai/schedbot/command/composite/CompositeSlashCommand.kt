package online.senpai.schedbot.command.composite

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import online.senpai.schedbot.command.CompositeClassDefinition
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

sealed interface CompositeSlashCommand {
    val enabled: Boolean
    val guilds: Flux<Long>
    val definition: CompositeClassDefinition

    interface Subcommand {
        val definition: ApplicationCommandOptionData
        fun handler(event: SlashCommandEvent): Mono<Void> = Mono.empty()
    }
}
