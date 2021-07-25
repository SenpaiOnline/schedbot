package online.senpai.schedbot.command.standalone

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import online.senpai.schedbot.command.FIRST_TEST_GUILD_ID
import online.senpai.schedbot.command.defineStandaloneCommand
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("unused")
object GreeterCommand : StandaloneSlashCommand {
    override val enabled: Boolean = true
    override val guilds: Flux<Long> = Flux.just(FIRST_TEST_GUILD_ID)
    override val definition: ApplicationCommandRequest = defineStandaloneCommand {
        name = "hello"
        description = "says hello to the given name"
        addOption {
            name = "name"
            description = "Introduce yourself"
            required = true
            type = ApplicationCommandOptionType.STRING
        }
    }

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
