package online.senpai.schedbot.mapper

import discord4j.common.util.Snowflake

class SnowflakeCustomMappings {
    fun map(value: Long): Snowflake = Snowflake.of(value)
    fun map(value: Snowflake): Long = value.asLong()
}
