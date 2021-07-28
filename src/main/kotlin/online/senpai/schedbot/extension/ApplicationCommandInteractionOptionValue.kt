package online.senpai.schedbot.extension

import discord4j.common.util.Snowflake
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import reactor.core.publisher.Mono

fun ApplicationCommandInteractionOptionValue.asMention(guild: Snowflake? = null): Mono<String> =
    this.client
        .getUserById(Snowflake.of(this.raw))
        .map { user: User -> user.mention }
        .onErrorResume {
            if (guild != null) {
                this.client
                    .getRoleById(guild, Snowflake.of(this.raw))
                    .map { role: Role -> role.mention }
            } else {
                Mono.error(IllegalArgumentException("Option value cannot be converted as mention"))
            }
        }
