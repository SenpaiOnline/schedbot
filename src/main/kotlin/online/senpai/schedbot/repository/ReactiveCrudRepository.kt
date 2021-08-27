package online.senpai.schedbot.repository

import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveCrudRepository<T, ID> {
    fun <S : T> save(entity: S): Mono<Void>

    fun <S : T> saveAll(entities: Iterable<S>): Flux<Void>

    fun <S : T> saveAll(entityStream: Publisher<S>): Flux<Void>

    fun findById(id: ID): Mono<T>

    fun findById(id: Publisher<ID>): Mono<T>

    fun existsById(id: ID): Mono<Boolean>

    fun existsById(id: Publisher<ID>): Mono<Boolean>

    fun findAll(): Flux<T>

    fun findAllById(ids: Iterable<ID>): Flux<T>

    fun findAllById(idStream: Publisher<ID>): Flux<T>

    fun count(): Mono<Long>

    fun deleteById(id: ID): Mono<Boolean>

    fun deleteById(id: Publisher<ID>): Mono<Boolean>

    fun delete(entity: T): Mono<Boolean>

    fun deleteAllById(ids: Iterable<ID>): Mono<Boolean>

    fun deleteAll(entities: Iterable<T>): Mono<Boolean>

    fun deleteAll(entityStream: Publisher<out T>): Mono<Boolean>

    fun deleteAll(): Mono<Boolean>
}

