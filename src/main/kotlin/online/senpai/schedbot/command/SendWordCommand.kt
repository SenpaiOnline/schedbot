package online.senpai.schedbot.command

import botrino.api.i18n.Translator
import botrino.command.Command
import botrino.command.CommandContext
import botrino.command.annotation.Alias
import botrino.command.annotation.TopLevelCommand
import botrino.command.cooldown.Cooldown
import botrino.command.doc.CommandDocumentation
import botrino.command.grammar.ArgumentMapper
import botrino.command.grammar.CommandGrammar
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.OffsetDateTime

@Suppress("unused")
@TopLevelCommand
@Alias("sendword")
class SendWordCommand : Command {
    private val grammar = CommandGrammar.builder()
        .nextArgument("word")
        .nextArgument("count", ArgumentMapper.asInteger())
        .nextArgument("date", ArgumentMapper.`as`(OffsetDateTime::parse))
        .nextArgument("channels", ArgumentMapper.asGuildChannel())
        .setVarargs(true)
        .build(Args::class.java)

    override fun run(ctx: CommandContext): Mono<Void> {
        return grammar.resolve(ctx)
            .flatMap { args ->
                Flux.fromIterable(args.channels)
                    .ofType(GuildMessageChannel::class.java)
                    .flatMap { channel ->
                        Flux.range(0, args.count)
                            .flatMap { channel.createMessage(args.word) }
                            .then()
                    }
                    .then()
            }
    }

    override fun cooldown(): Cooldown = Cooldown.of(1, Duration.ofSeconds(10))

    override fun documentation(translator: Translator): CommandDocumentation {
        return CommandDocumentation.builder()
            .setSyntax("[command...]")
            .setDescription("Displays helpful info on commands.")
            .setBody("Without arguments, gives a list of available commands. Pass a command or a sequence " +
                    "of subcommands in arguments to get detailed information on that specific command/subcommand.")
            .build()
    }

    private data class Args(
        val word: String, val count: Int, val channels: List<GuildChannel>
    )
}

fun <T> ArgumentMapper<T>.asOffsetDateTime(): ArgumentMapper<OffsetDateTime> {
    return ArgumentMapper.`as`(OffsetDateTime::parse)
}
