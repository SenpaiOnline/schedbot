package online.senpai.schedbot.module

import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import online.senpai.schedbot.entity.ScheduledEvent
import online.senpai.schedbot.mapper.ScheduledMessagesMapper
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mapstruct.factory.Mappers
import java.time.OffsetDateTime

val mappersModule: Module = module {
    single<ScheduledMessagesMapper> { Mappers.getMapper(ScheduledMessagesMapper::class.java) }
    single<(Row, RowMetadata) -> ScheduledEvent> {
        { row: Row, _: RowMetadata ->
            ScheduledEvent(
                id = row["ID"] as Long,
                createdOn = row["CREATED_ON", OffsetDateTime::class.java],
                fireOn = row["FIRE_ON", OffsetDateTime::class.java],
                channelId = row["CHANNEL_ID"] as Long,
                authorId = row["AUTHOR_ID"] as Long,
                payload = row["PAYLOAD"] as String
            )
        }
    }
}
