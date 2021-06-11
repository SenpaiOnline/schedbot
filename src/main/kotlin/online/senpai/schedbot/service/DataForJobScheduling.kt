package online.senpai.schedbot.service

import java.time.OffsetDateTime

data class DataForJobScheduling(
    val messageChannel: Long,
    val author: Long,
    val rawMessage: String,
    val delayUntil: OffsetDateTime
)
