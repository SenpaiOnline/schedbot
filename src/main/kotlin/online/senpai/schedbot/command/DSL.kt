package online.senpai.schedbot.command

import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import online.senpai.schedbot.command.composite.CompositeSlashCommand

class SubcommandCommandBuilder {
    private val optionBuilder = OptionBuilder()
    var name = ""
    var description = ""

    fun addOption(init: OptionBuilder.() -> Unit): Boolean = optionBuilder.apply(init).addOption()

    fun build(): ApplicationCommandOptionData =
        ApplicationCommandOptionData
            .builder()
            .description(description)
            .name(name.lowercase())
            .type(ApplicationCommandOptionType.SUB_COMMAND.value)
            .addAllOptions(optionBuilder.build())
            .build()
}

class OptionBuilder {
    private val options: MutableSet<ApplicationCommandOptionData> = mutableSetOf()
    var name = ""
    var description = ""
    var required = false
    var type: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING

    fun addOption(): Boolean =
        options.add(
            ApplicationCommandOptionData
                .builder()
                .name(name.lowercase())
                .description(description)
                .required(required)
                .type(type.value)
                .build()
        )

    fun build(): MutableList<ApplicationCommandOptionData> = options.toMutableList()
}

data class CompositeClassDefinition(
    val slashApiDefinition: ApplicationCommandRequest,
    val subcommands: Set<CompositeSlashCommand.Subcommand>
)

class SubcommandBuilder {
    private val subcommands: MutableSet<CompositeSlashCommand.Subcommand> = mutableSetOf()
    lateinit var subcommand: CompositeSlashCommand.Subcommand

    fun addSubcommand(): Boolean = subcommands.add(subcommand)

    fun build(): Set<CompositeSlashCommand.Subcommand> = subcommands
}

class CompositeCommandBuilder {
    private val subcommandBuilder = SubcommandBuilder()
    var name = ""
    var description = ""

    fun addSubcommand(init: SubcommandBuilder.() -> Unit): Boolean =
        subcommandBuilder.apply(init).addSubcommand()

    fun build(): CompositeClassDefinition {
        val subcommands: Set<CompositeSlashCommand.Subcommand> = subcommandBuilder.build()
        val definition: ApplicationCommandRequest = ApplicationCommandRequest
            .builder()
            .name(name.lowercase())
            .description(description)
            .addAllOptions(subcommands.map { it.definition })
            .build()
        return CompositeClassDefinition(definition, subcommands)
    }
}

class StandaloneCommandBuilder {
    private val optionBuilder = OptionBuilder()
    var name = ""
    var description = ""

    fun addOption(init: OptionBuilder.() -> Unit): Boolean = optionBuilder.apply(init).addOption()

    fun build(): ApplicationCommandRequest =
        ApplicationCommandRequest
            .builder()
            .description(description)
            .name(name.lowercase())
            .addAllOptions(optionBuilder.build())
            .build()
}

fun defineStandaloneCommand(init: StandaloneCommandBuilder.() -> Unit): ApplicationCommandRequest {
    return StandaloneCommandBuilder().apply(init).build()
}

fun defineCompositeCommand(init: CompositeCommandBuilder.() -> Unit): CompositeClassDefinition {
    return CompositeCommandBuilder().apply(init).build()
}

fun defineSubcommand(init: SubcommandCommandBuilder.() -> Unit): ApplicationCommandOptionData {
    return SubcommandCommandBuilder().apply(init).build()
}
