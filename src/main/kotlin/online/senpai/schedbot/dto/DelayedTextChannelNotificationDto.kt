package online.senpai.schedbot.dto

import discord4j.common.util.Snowflake
import java.time.OffsetDateTime

data class DelayedTextChannelNotificationDto(
    val id: Long,
    val createdOn: OffsetDateTime,
    val fireOn: OffsetDateTime,
    val channelId: Snowflake,
    val authorId: Snowflake,
    val payload: String
)
