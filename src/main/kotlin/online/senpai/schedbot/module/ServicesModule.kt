package online.senpai.schedbot.module

import online.senpai.schedbot.service.SchedulingService
import online.senpai.schedbot.service.SchedulingServiceImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val servicesModule: Module = module {
    single<SchedulingService> { SchedulingServiceImpl() }
}
