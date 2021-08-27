package online.senpai.schedbot.extension

import io.r2dbc.spi.Result
import io.r2dbc.spi.Statement
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun Statement.executeSingle(): Mono<out Result> =
    Mono.from(this.execute())

fun Statement.executeMany(): Flux<out Result> =
    Flux.from(this.execute())
