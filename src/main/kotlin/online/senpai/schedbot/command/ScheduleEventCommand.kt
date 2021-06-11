package online.senpai.schedbot.command

import botrino.command.Command
import botrino.command.CommandContext
import botrino.command.CommandFailedException
import botrino.command.Scope
import botrino.command.annotation.Alias
import botrino.command.annotation.TopLevelCommand
import botrino.command.grammar.ArgumentMapper
import botrino.command.grammar.CommandGrammar
import com.github.alex1304.rdi.finder.annotation.RdiFactory
import com.github.alex1304.rdi.finder.annotation.RdiService
import discord4j.core.`object`.entity.Role
import online.senpai.schedbot.service.DataForJobScheduling
import online.senpai.schedbot.service.SchedulingService
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.OffsetDateTime

@Suppress("unused")
@RdiService
@TopLevelCommand
@Alias("sched")
class ScheduleEventCommand @RdiFactory constructor(
    private val schedulingService: SchedulingService
): Command {
    private val grammar = CommandGrammar
        .builder()
        .nextArgument("date", ArgumentMapper.`as`(OffsetDateTime::parse))
        .nextArgument("role", ArgumentMapper.asGuildRole())
//        .beginOptionalArguments()
        .nextArgument("message")
        .build(Args::class.java)

    override fun run(ctx: CommandContext): Mono<Void> {
        return grammar
            .resolve(ctx)
            /*.handle { args: Args, sink: SynchronousSink<*> ->
                val timeNow = OffsetDateTime.now()
                if (args.date.isAfter(timeNow)) {
                    sink.(DataForJobScheduling(
                        messageChannel = ctx.channel(),
                        rawMessage = "Hello world",
                        delay = Duration.ofSeconds(args.date.toEpochSecond() - timeNow.toEpochSecond())
                    ))
                } else {
                    sink.error(CommandFailedException("Planned date must be greater than the current date!"))
                }
            }*/
            .flatMap { args: Args ->
                val timeNow = OffsetDateTime.now()
                if (args.date.isAfter(timeNow)) {
                    schedulingService.scheduleNewJob(
                        DataForJobScheduling(
                            messageChannel = ctx.channel().id.asLong(),
                            author = ctx.author().id.asLong(),
                            rawMessage = "${args.role.mention} ${args.message}",
                            delayUntil = args.date
                        )
                    )
                } else {
                    Mono.error(CommandFailedException("Planned date must be greater than the current date!"))
                }
            }
            /*.and { ctx.channel().createMessage("The task's been added to the scheduler successfully").subscribe() }*/
            .thenEmpty { ctx.channel().createMessage("The task's been added to the scheduler successfully").subscribe() }
        /*return grammar
            .resolve(ctx)
            .flatMap { args ->
                Flux.fromIterable(args.roles)
                    .ofType(GuildMessageChannel::class.java)
                    .flatMap { channel ->
                        Flux.range(0, args.count)
                            .flatMap { channel.createMessage(args.word) }
                            .then()
                    }
                    .then()
            }*/
    }

    override fun subcommands(): MutableSet<Command> =
        mutableSetOf(
            Command
                .builder("list") { ctx: CommandContext ->
                    ctx
                        .channel()
                        .createMessage("Scheduled jobs: \n${schedulingService.runningJobs.joinToString(separator = "\n")}")
                        .then()
                }
                .inheritFrom(this)
                .build()
        )

    override fun scope(): Scope = Scope.GUILD_ONLY

    class Args {
        lateinit var date: OffsetDateTime
        lateinit var role: Role
        lateinit var message: String
        val duration: Duration by lazy(LazyThreadSafetyMode.NONE) {
            Duration.ofSeconds(date.toEpochSecond() - OffsetDateTime.now().toEpochSecond())
        }
    }
}
