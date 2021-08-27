package online.senpai.schedbot.repository

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import online.senpai.schedbot.entity.ScheduledEvent
import online.senpai.schedbot.extension.executeMany
import online.senpai.schedbot.extension.executeSingle
import online.senpai.schedbot.extension.getRowsUpdateSingle
import online.senpai.schedbot.extension.mapSingle
import online.senpai.schedbot.extension.withConnection
import online.senpai.schedbot.extension.withConnectionMany
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ScheduledRepositoryImpl : ScheduledRepository, KoinComponent {
    private val connectionPool: ConnectionPool by inject()
    private val mapper: (Row, RowMetadata) -> ScheduledEvent by inject()

    init {
        connectionPool.withConnection {
            createStatement(
                """
                    CREATE TABLE IF NOT EXISTS scheduled_events 
                    (
                        ID         BIGINT                   NOT NULL PRIMARY KEY,
                        CREATED_ON TIMESTAMP WITH TIME ZONE NOT NULL,
                        FIRE_ON    TIMESTAMP WITH TIME ZONE NOT NULL,
                        CHANNEL_ID BIGINT                   NOT NULL,
                        AUTHOR_ID  BIGINT                   NOT NULL,
                        PAYLOAD    VARCHAR(255)             NOT NULL
                    ) 
                """.trimIndent()
            )
                .executeSingle()
        }
            .subscribe()
    }

    override fun <S : ScheduledEvent> save(entity: S): Mono<Void> =
        connectionPool.withConnection {
            createStatement(
                """
                INSERT INTO SCHEDULED_EVENTS (ID, CREATED_ON, FIRE_ON, CHANNEL_ID, AUTHOR_ID, PAYLOAD) 
                VALUES ($1, $2, $3, $4, $5, $6)
                """.trimIndent()
            )
                .bind("$1", entity.id)
                .bind("$2", entity.createdOn)
                .bind("$3", entity.fireOn)
                .bind("$4", entity.channelId)
                .bind("$5", entity.authorId)
                .bind("$6", entity.payload)
                .executeSingle()
                .then()
        }

    override fun <S : ScheduledEvent> saveAll(entities: Iterable<S>): Flux<Void> {
        TODO("Not yet implemented")
    }

    override fun <S : ScheduledEvent> saveAll(entityStream: Publisher<S>): Flux<Void> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Mono<ScheduledEvent> =
        connectionPool.withConnection {
            createStatement(
                """
                SELECT TOP 1 ID, CREATED_ON, FIRE_ON, CHANNEL_ID, AUTHOR_ID, PAYLOAD 
                FROM SCHEDULED_EVENTS WHERE ID = $1
                """.trimIndent()
            )
                .bind("$1", id)
                .executeSingle()
                .flatMap { result: Result ->
                    result.mapSingle(mapper::invoke)
                }
        }

    override fun findById(id: Publisher<Long>): Mono<ScheduledEvent> {
        TODO("Not yet implemented")
    }

    override fun existsById(id: Long): Mono<Boolean> =
        connectionPool.withConnection {
            createStatement("SELECT TOP 1 1 FROM SCHEDULED_EVENTS WHERE ID = $1")
                .bind("$1", id)
                .executeSingle()
                .flatMap { result: Result ->
                    result.mapSingle { row: Row, _: RowMetadata -> row }
                }
                .hasElement()
        }

    override fun existsById(id: Publisher<Long>): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Flux<ScheduledEvent> =
        connectionPool.withConnectionMany {
            createStatement("SELECT ID, CREATED_ON, FIRE_ON, CHANNEL_ID, AUTHOR_ID, PAYLOAD FROM SCHEDULED_EVENTS")
                .executeMany()
                .flatMap { result: Result ->
                    result.map(mapper::invoke)
                }
        }

    override fun findAllById(ids: Iterable<Long>): Flux<ScheduledEvent> {
        TODO("Not yet implemented")
    }

    override fun findAllById(idStream: Publisher<Long>): Flux<ScheduledEvent> {
        TODO("Not yet implemented")
    }

    override fun count(): Mono<Long> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long): Mono<Boolean> =
        connectionPool.withConnection {
            createStatement("DELETE FROM SCHEDULED_EVENTS WHERE ID = $1")
                .bind("$1", id)
                .executeSingle()
                .flatMap(Result::getRowsUpdateSingle)
                .hasElement()
        }

    override fun deleteById(id: Publisher<Long>): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun delete(entity: ScheduledEvent): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: Iterable<Long>): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: Iterable<ScheduledEvent>): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entityStream: Publisher<out ScheduledEvent>): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(): Mono<Boolean> {
        TODO("Not yet implemented")
    }
}
