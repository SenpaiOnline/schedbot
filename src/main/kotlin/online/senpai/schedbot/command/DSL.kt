package online.senpai.schedbot.command

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import online.senpai.schedbot.command.composite.CompositeSlashCommand

@DslMarker
annotation class SlashCommandDsl

@SlashCommandDsl
class OptionBuilder {
    private val choices: MutableList<Pair<String, Any>> = mutableListOf()
    var name = ""
    var description = ""
    var required = false
    var type: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING

    fun choices(init: ChoiceBuilder.() -> Unit): Boolean = choices.addAll(ChoiceBuilder().apply(init).build())

    internal fun build(): ApplicationCommandOptionData =
        ApplicationCommandOptionData
            .builder()
            .name(name.lowercase())
            .description(description)
            .required(required)
            .type(type.value)
            .choices(choices.map { (name: String, value: Any) ->
                ApplicationCommandOptionChoiceData
                    .builder()
                    .name(name)
                    .value(value)
                    .build()
            })
            .build()
}

@SlashCommandDsl
class ChoiceBuilder {
    private val choices: MutableList<Pair<String, Any>> = mutableListOf()

    infix fun String.to(value: Any): Boolean = choices.add(Pair(this, value))

    fun build(): MutableList<Pair<String, Any>> = choices
}

data class CompositeClassDefinition(
    val slashApiDefinition: ApplicationCommandRequest,
    val subcommands: Set<CompositeSlashCommand.Subcommand>
)

@SlashCommandDsl
class SubcommandBuilder {
    private val subcommands: MutableList<CompositeSlashCommand.Subcommand> = mutableListOf()

    operator fun CompositeSlashCommand.Subcommand.unaryPlus(): Boolean = subcommands.add(this)

    internal fun build(): Set<CompositeSlashCommand.Subcommand> = subcommands.toSet()
}

@SlashCommandDsl
class SubcommandCommandBuilder {
    private val options: MutableList<ApplicationCommandOptionData> = mutableListOf()
    var name = ""
    var description = ""

    fun option(init: OptionBuilder.() -> Unit): Boolean = options.add(OptionBuilder().apply(init).build())

    internal fun build(): ApplicationCommandOptionData =
        ApplicationCommandOptionData
            .builder()
            .description(description)
            .name(name.lowercase())
            .type(ApplicationCommandOptionType.SUB_COMMAND.value)
            .addAllOptions(options)
            .build()
}

@SlashCommandDsl
class CompositeCommandBuilder {
    private val subcommands: MutableList<CompositeSlashCommand.Subcommand> = mutableListOf()
    var name = ""
    var description = ""

    fun subcommands(init: SubcommandBuilder.() -> Unit): Boolean =
        subcommands.addAll(SubcommandBuilder().apply(init).build())

    internal fun build(): CompositeClassDefinition = CompositeClassDefinition(
        slashApiDefinition = ApplicationCommandRequest
            .builder()
            .name(name.lowercase())
            .description(description)
            .addAllOptions(subcommands.map(CompositeSlashCommand.Subcommand::definition))
            .build(),
        subcommands = subcommands.toSet()
    )
}

@SlashCommandDsl
class StandaloneCommandBuilder {
    private val options: MutableList<ApplicationCommandOptionData> = mutableListOf()
    var name = ""
    var description = ""

    fun option(init: OptionBuilder.() -> Unit): Boolean = options.add(OptionBuilder().apply(init).build())

    internal fun build(): ApplicationCommandRequest =
        ApplicationCommandRequest
            .builder()
            .description(description)
            .name(name.lowercase())
            .addAllOptions(options)
            .build()
}

fun defineStandaloneCommand(init: StandaloneCommandBuilder.() -> Unit): ApplicationCommandRequest =
    StandaloneCommandBuilder().apply(init).build()

fun defineCompositeCommand(init: CompositeCommandBuilder.() -> Unit): CompositeClassDefinition =
    CompositeCommandBuilder().apply(init).build()

fun defineSubcommand(init: SubcommandCommandBuilder.() -> Unit): ApplicationCommandOptionData =
    SubcommandCommandBuilder().apply(init).build()
