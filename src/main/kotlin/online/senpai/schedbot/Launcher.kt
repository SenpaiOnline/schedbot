package online.senpai.schedbot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.long
import online.senpai.schedbot.module.databaseModule
import online.senpai.schedbot.module.discordClientModule
import online.senpai.schedbot.module.handlersModule
import online.senpai.schedbot.module.mappersModule
import online.senpai.schedbot.module.repositoriesModule
import online.senpai.schedbot.module.servicesModule
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.logger.slf4jLogger

private const val DEFAULT_INTENTS: Long = 32509

object Launcher : CliktCommand() {
    private val token: String by option("-t", envvar = "TOKEN").required()
    private val intents: Long by option("--intents", envvar = "INTENTS").long().default(DEFAULT_INTENTS)
    private val koinLogLevel: Level by option("--koin-log-level").enum<Level>(ignoreCase = true).default(Level.INFO)

    override fun run() {
        startKoin {
            slf4jLogger(level = koinLogLevel)
            modules(
                discordClientModule(token, intents),
                servicesModule,
                handlersModule,
                databaseModule,
                mappersModule,
                repositoriesModule
            )
        }
        Application.runBlocking()
    }
}

fun main(args: Array<String>): Unit = Launcher.main(args)
