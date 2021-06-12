package online.senpai.schedbot.command

import discord4j.discordjson.json.ImmutableApplicationCommandRequest

sealed interface SlashCommand {
    val commandRequest: ImmutableApplicationCommandRequest
}
