package online.senpai.schedbot.handler

import botrino.api.annotation.Primary
import botrino.api.util.DurationUtils
import botrino.command.CommandContext
import botrino.command.CommandErrorHandler
import botrino.command.CommandFailedException
import botrino.command.InvalidSyntaxException
import botrino.command.cooldown.CooldownException
import botrino.command.privilege.PrivilegeException
import io.honeybadger.reporter.HoneybadgerReporter
import reactor.core.publisher.Mono


@Suppress("unused")
@Primary
class CommandErrorHandlerImpl : CommandErrorHandler {
    private val honeybadgerReporter = HoneybadgerReporter()

    override fun handleCommandFailed(
        e: CommandFailedException,
        ctx: CommandContext
    ): Mono<Void> = ctx
        .channel()
        .createMessage("\uD83D\uDEAB ${e.message}")
        .then()

    override fun handleInvalidSyntax(e: InvalidSyntaxException, ctx: CommandContext): Mono<Void> {
        val badArgName: String? = e.badArgumentName.orElse(null)
        val badArgValue: String? = e.badArgumentValue.orElse(null)
        val message: String = when {
            badArgName == null && badArgValue == null -> "Expected a subcommand."
            badArgName == null -> "Subcommand \"$badArgValue\" not found."
            badArgValue == null -> "Missing argument `<$badArgName>`!"
            else -> "Value \"$badArgValue\" has incorrect type for argument `<$badArgName>`!"
        }
        return ctx.channel().createMessage(message).then()
    }

    override fun handlePrivilege(
        e: PrivilegeException,
        ctx: CommandContext
    ): Mono<Void> = ctx
        .channel()
        .createMessage("You have insufficient privileges to run this command.")
        .then()

    override fun handleCooldown(
        e: CooldownException,
        ctx: CommandContext
    ): Mono<Void> = ctx
        .channel()
        .createMessage("You are on cooldown. Retry in ${DurationUtils.format(e.retryAfter)}")
        .then()

    override fun handleDefault(
        t: Throwable,
        ctx: CommandContext
    ): Mono<Void> = ctx
        .channel()
        .createMessage(
            """
                |Something went wrong!
                |```java
                |${t.stackTraceToString().take(1900)}
                |```
            """.trimMargin()
        )
        .and {
            Mono
                .fromRunnable<Void> { honeybadgerReporter.reportError(t, ctx) }
                .subscribeOn(ctx.channel().client.coreResources.reactorResources.blockingTaskScheduler)
                .subscribe()
        }
        .onErrorResume { e: Throwable ->
            t.addSuppressed(e)
            Mono.empty()
        }
        .then(Mono.error(t)) // Forward downstream
}
