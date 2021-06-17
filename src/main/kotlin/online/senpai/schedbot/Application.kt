package online.senpai.schedbot

import online.senpai.schedbot.handler.EventHandler
import online.senpai.schedbot.handler.SlashCommandsDispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Application : KoinComponent {
    private val slashCommandsDispatcher: SlashCommandsDispatcher by inject()
    private val eventHandler: EventHandler by inject()

    fun runBlocking() {
        // TODO better error handling
        Runtime.getRuntime().addShutdownHook(Thread { slashCommandsDispatcher.destroy().block() })
        slashCommandsDispatcher.initialize().subscribe()
        eventHandler.initialize().subscribe()
        eventHandler.awaitTermination().block()
    }
}
