package online.senpai.schedbot.module

import online.senpai.schedbot.handler.EventHandler
import online.senpai.schedbot.handler.EventHandlerImpl
import online.senpai.schedbot.handler.SlashCommandsDispatcher
import online.senpai.schedbot.handler.SlashCommandsDispatcherImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val handlersModule: Module = module {
    single<SlashCommandsDispatcher> { SlashCommandsDispatcherImpl() }
    single<EventHandler> { EventHandlerImpl() }
}
