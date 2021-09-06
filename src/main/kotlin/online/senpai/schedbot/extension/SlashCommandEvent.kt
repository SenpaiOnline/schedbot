package online.senpai.schedbot.extension

import discord4j.common.util.Snowflake
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.event.domain.interaction.SlashCommandEvent
import reactor.core.publisher.Mono

fun <T> SlashCommandEvent.getOptionAs(
    transform: (ApplicationCommandInteractionOptionValue) -> T,
    optionName: String
): T? = this
    .interaction
    .commandInteraction
    .orElse(null)
    ?.getOption(optionName)
    ?.flatMap(ApplicationCommandInteractionOption::getValue)
    ?.map(transform)
    ?.orElse(null)

fun <T> SlashCommandEvent.getSubcommandOptionAs(
    transform: (ApplicationCommandInteractionOptionValue) -> T,
    optionName: String
): T? = this
    .interaction
    .commandInteraction
    .orElse(null)
    ?.options
    ?.firstOrNull()
    ?.getOption(optionName)
    ?.flatMap(ApplicationCommandInteractionOption::getValue)
    ?.map(transform)
    ?.orElse(null)

private fun <T> SlashCommandEvent.getOptionAs(
    transform: (ApplicationCommandInteractionOptionValue) -> T,
    optionName: String,
    defaultValue: T
): T = this
    .interaction
    .commandInteraction
    .orElse(null)
    ?.getOption(optionName)
    ?.flatMap(ApplicationCommandInteractionOption::getValue)
    ?.map(transform)
    ?.orElse(null)
    ?: defaultValue

private fun <T> SlashCommandEvent.getSubcommandOptionAs(
    transform: (ApplicationCommandInteractionOptionValue) -> T,
    optionName: String,
    defaultValue: T
): T = this
    .interaction
    .commandInteraction
    .orElse(null)
    ?.options
    ?.firstOrNull()
    ?.getOption(optionName)
    ?.flatMap(ApplicationCommandInteractionOption::getValue)
    ?.map(transform)
    ?.orElse(null)
    ?: defaultValue

/*RAW BEGINS*/
fun SlashCommandEvent.getOptionAsRawOrNull(optionName: String): String? =
    getOptionAs(ApplicationCommandInteractionOptionValue::getRaw, optionName)

fun SlashCommandEvent.getSubcommandOptionAsRawOrNull(optionName: String): String? =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::getRaw, optionName)

fun SlashCommandEvent.getOptionAsRawOrElse(optionName: String, defaultValue: String): String =
    getOptionAs(ApplicationCommandInteractionOptionValue::getRaw, optionName, defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsRawOrElse(optionName: String, defaultValue: String): String =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::getRaw, optionName, defaultValue)
/*RAW ENDS*/

/*STRING BEGINS*/
fun SlashCommandEvent.getOptionAsStringOrNull(optionName: String): String? =
    getOptionAs(ApplicationCommandInteractionOptionValue::asString, optionName)

fun SlashCommandEvent.getSubcommandOptionAsStringOrNull(optionName: String): String? =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asString, optionName)

fun SlashCommandEvent.getOptionAsStringOrElse(optionName: String, defaultValue: String): String =
    getOptionAs(ApplicationCommandInteractionOptionValue::asString, optionName, defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsStringOrElse(optionName: String, defaultValue: String): String =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asString, optionName, defaultValue)
/*STRING ENDS*/

/*BOOLEAN BEGINS*/
fun SlashCommandEvent.getOptionAsBooleanOrNull(optionName: String): Boolean? =
    getOptionAs(ApplicationCommandInteractionOptionValue::asBoolean, optionName)

fun SlashCommandEvent.getSubcommandOptionAsBooleanOrNull(optionName: String): Boolean? =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asBoolean, optionName)

fun SlashCommandEvent.getOptionAsBooleanOrElse(optionName: String, defaultValue: Boolean): Boolean =
    getOptionAs(ApplicationCommandInteractionOptionValue::asBoolean, optionName, defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsBooleanOrElse(optionName: String, defaultValue: Boolean): Boolean =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asBoolean, optionName, defaultValue)
/*BOOLEAN ENDS*/

/*LONG BEGINS*/
fun SlashCommandEvent.getOptionAsLongOrNull(optionName: String): Long? =
    getOptionAs(ApplicationCommandInteractionOptionValue::asLong, optionName)

fun SlashCommandEvent.getSubcommandOptionAsLongOrNull(optionName: String): Long? =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asLong, optionName)

fun SlashCommandEvent.getOptionAsLongOrElse(optionName: String, defaultValue: Long): Long =
    getOptionAs(ApplicationCommandInteractionOptionValue::asLong, optionName, defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsLongOrElse(optionName: String, defaultValue: Long): Long =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asLong, optionName, defaultValue)
/*LONG ENDS*/

/*SNOWFLAKE BEGINS*/
fun SlashCommandEvent.getOptionAsSnowflakeOrNull(optionName: String): Snowflake? =
    getOptionAs(ApplicationCommandInteractionOptionValue::asSnowflake, optionName)

fun SlashCommandEvent.getSubcommandOptionAsSnowflakeOrNull(optionName: String): Snowflake? =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asSnowflake, optionName)

fun SlashCommandEvent.getOptionAsSnowflakeOrElse(optionName: String, defaultValue: Snowflake): Snowflake =
    getOptionAs(ApplicationCommandInteractionOptionValue::asSnowflake, optionName, defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsSnowflakeOrElse(optionName: String, defaultValue: Snowflake): Snowflake =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asSnowflake, optionName, defaultValue)
/*SNOWFLAKE ENDS*/

/*USER BEGINS*/
fun SlashCommandEvent.getOptionAsUser(optionName: String, defaultUser: Mono<User> = Mono.empty()): Mono<User> =
    getOptionAs(ApplicationCommandInteractionOptionValue::asUser, optionName, defaultUser)

fun SlashCommandEvent.getSubcommandOptionAsUser(
    optionName: String,
    defaultUser: Mono<User> = Mono.empty()
): Mono<User> =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asUser, optionName, defaultUser)
/*USER ENDS*/

/*ROLE BEGINS*/
fun SlashCommandEvent.getOptionAsRole(optionName: String, defaultRole: Mono<Role> = Mono.empty()): Mono<Role> =
    getOptionAs(ApplicationCommandInteractionOptionValue::asRole, optionName, defaultRole)

fun SlashCommandEvent.getSubcommandOptionAsRole(
    optionName: String,
    defaultRole: Mono<Role> = Mono.empty()
): Mono<Role> =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asRole, optionName, defaultRole)
/*ROLE ENDS*/

/*CHANNEL BEGINS*/
fun SlashCommandEvent.getOptionAsChannel(
    optionName: String,
    defaultChannel: Mono<Channel> = Mono.empty()
): Mono<Channel> =
    getOptionAs(ApplicationCommandInteractionOptionValue::asChannel, optionName, defaultChannel)

fun SlashCommandEvent.getSubcommandOptionAsChannel(
    optionName: String,
    defaultChannel: Mono<Channel> = Mono.empty()
): Mono<Channel> =
    getSubcommandOptionAs(ApplicationCommandInteractionOptionValue::asChannel, optionName, defaultChannel)
/*CHANNEL ENDS*/

/*MENTION BEGINS*/
fun SlashCommandEvent.getOptionAsMention(optionName: String): Mono<String> =
    getOptionAs(
        optionName = optionName,
        defaultValue = Mono.empty(),
        transform = { optionValue: ApplicationCommandInteractionOptionValue ->
            optionValue.asMention(this.interaction.guildId.orElse(null))
        }
    )

fun SlashCommandEvent.getOptionAsMention(optionName: String, defaultValue: String): Mono<String> =
    getOptionAsMention(optionName).defaultIfEmpty(defaultValue)

fun SlashCommandEvent.getSubcommandOptionAsMention(optionName: String): Mono<String> =
    getSubcommandOptionAs(
        optionName = optionName,
        defaultValue = Mono.empty(),
        transform = { optionValue: ApplicationCommandInteractionOptionValue ->
            optionValue.asMention(this.interaction.guildId.orElse(null))
        }
    )

fun SlashCommandEvent.getSubcommandOptionAsMention(optionName: String, defaultValue: String): Mono<String> =
    getSubcommandOptionAsMention(optionName).defaultIfEmpty(defaultValue)
/*MENTION ENDS*/
