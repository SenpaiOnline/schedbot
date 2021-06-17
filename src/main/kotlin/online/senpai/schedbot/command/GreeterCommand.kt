package online.senpai.schedbot.command

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import reactor.core.publisher.Mono

private const val TEST_GUILD_ID = 803383152688365599L

@Suppress("unused")
object GreeterCommand : SlashCommand {
    override val enabled: Boolean = true
    override val scope = SlashCommand.Scope.Guild(TEST_GUILD_ID)
    override val commandRequest: ApplicationCommandRequest = ApplicationCommandRequest.builder()
        .name("hello")
        .description("says hello to the given name")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("name")
                .description("Introduce yourself")
                .type(ApplicationCommandOptionType.STRING.value)
                .required(true)
                .build()
        )
        .build()

    override fun handler(event: InteractionCreateEvent): Mono<Void> =
        event
            .acknowledge()
            .then(
                event
                    .interactionResponse
                    .createFollowupMessage(
                        event
                            .interaction
                            .commandInteraction
                            .getOption("name")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .map { "Hello $it!" }
                            .orElse("")
                    )
                    .then()
            )
}
