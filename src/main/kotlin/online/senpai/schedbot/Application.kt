package online.senpai.schedbot

import io.r2dbc.pool.ConnectionPool
import online.senpai.schedbot.handler.EventHandler
import online.senpai.schedbot.handler.SlashCommandsDispatcher
import online.senpai.schedbot.service.SchedulingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono

object Application : KoinComponent {
    private val slashCommandsDispatcher: SlashCommandsDispatcher by inject()
    private val eventHandler: EventHandler by inject()
    private val h2ConnectionPool: ConnectionPool by inject()
    private val schedulingService: SchedulingService by inject()

    fun runBlocking() {
        // TODO better error handling
        Runtime.getRuntime().addShutdownHook(Thread {
            slashCommandsDispatcher.destroy().block()
            h2ConnectionPool.disposeLater().block()
        })
        Mono
            .`when`(
                h2ConnectionPool.warmup(),
                schedulingService.initialize(),
                slashCommandsDispatcher.initialize(),
                eventHandler.initialize()
            )
            .subscribe()
        eventHandler.awaitTermination().block()
    }
}
