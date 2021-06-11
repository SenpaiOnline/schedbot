package online.senpai.schedbot.command

import botrino.api.util.EmojiManager
import botrino.command.Command
import botrino.command.CommandContext
import botrino.command.Scope
import botrino.command.annotation.Alias
import botrino.command.annotation.TopLevelCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.rest.util.AllowedMentions
/*import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional*/
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

@Suppress("unused")
@TopLevelCommand
@Alias("ping")
class PingCommand : Command {
    override fun run(ctx: CommandContext): Mono<Void> {
        val args = Args()
        args.main(ctx.input().arguments)
        val name = args.name
        val discord = ctx.event().client
        return Mono
            .just("803383152688365599")
            .map(Snowflake::of)
            .flatMap { guildSnowflake: Snowflake ->
                discord
                    .getGuildChannels(guildSnowflake)
                    .filter { channel: GuildChannel ->
                        channel.name == "abc" && channel.type == Channel.Type.GUILD_TEXT
                    }
                    .next()
                    .cast(GuildMessageChannel::class.java)
                    .flatMap { messageChannel: GuildMessageChannel ->
                        messageChannel.createMessage(name)
                    }
            }
            .then()
        /*val parser = ArgParser("ping")
        val name by parser.argument(ArgType.String).optional().default("world")
        parser.parse(ctx.input().arguments.toTypedArray())
        return ctx
            .channel()
            .createMessage(Base64.getUrlEncoder().encodeToString(ctx.event().message.id.asString().toByteArray()))
            .then()*/
    }

    override fun scope(): Scope = Scope.GUILD_ONLY

    class Args : NoOpCliktCommand() {
        val name by argument().default("world")
    }
}
