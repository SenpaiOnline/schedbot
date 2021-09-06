package online.senpai.schedbot.extension

import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import reactor.core.publisher.Mono

fun <T> Result.mapSingle(mappingFunction: (Row, RowMetadata) -> T): Mono<T> =
    Mono.from(this.map(mappingFunction::invoke))

fun Result.getRowsUpdateSingle(): Mono<Int> =
    Mono.from(this.rowsUpdated)
