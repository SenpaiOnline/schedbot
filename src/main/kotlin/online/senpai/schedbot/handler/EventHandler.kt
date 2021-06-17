package online.senpai.schedbot.handler

import reactor.core.publisher.Mono

interface EventHandler {
    fun initialize(): Mono<Void>
    fun awaitTermination(): Mono<Void>
}
