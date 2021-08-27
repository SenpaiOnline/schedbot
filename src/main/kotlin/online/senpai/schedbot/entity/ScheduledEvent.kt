package online.senpai.schedbot.entity

import java.time.OffsetDateTime

data class ScheduledEvent(
    var id: Long,
    var createdOn: OffsetDateTime,
    var fireOn: OffsetDateTime,
    var channelId: Long,
    var authorId: Long,
    var payload: String
)
