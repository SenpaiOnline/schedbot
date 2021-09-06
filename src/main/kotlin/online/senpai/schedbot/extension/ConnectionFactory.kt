package online.senpai.schedbot.extension

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun <T> ConnectionFactory.withConnection(function: Connection.() -> Mono<T>): Mono<T> =
    Mono.usingWhen(this.create(), function::invoke, Connection::close)

fun <T> ConnectionFactory.withConnectionMany(function: Connection.() -> Flux<T>): Flux<T> =
    Flux.usingWhen(this.create(), function::invoke, Connection::close)
