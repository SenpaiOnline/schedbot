package online.senpai.schedbot.mapper

import online.senpai.schedbot.dto.DelayedTextChannelNotificationDto
import online.senpai.schedbot.entity.ScheduledEvent
import org.mapstruct.Mapper

@Mapper(uses = [SnowflakeCustomMappings::class])
interface ScheduledMessagesMapper {
    fun transformToDto(entity: ScheduledEvent): DelayedTextChannelNotificationDto
    fun transformToEntity(dto: DelayedTextChannelNotificationDto): ScheduledEvent
}
