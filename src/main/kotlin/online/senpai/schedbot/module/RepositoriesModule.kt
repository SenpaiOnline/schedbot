package online.senpai.schedbot.module

import online.senpai.schedbot.repository.ScheduledRepository
import online.senpai.schedbot.repository.ScheduledRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoriesModule: Module = module {
    single<ScheduledRepository> { ScheduledRepositoryImpl() }
}
