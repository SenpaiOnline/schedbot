package online.senpai.schedbot.module

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.EventDispatcher
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.gateway.intent.IntentSet
import org.koin.core.module.Module
import org.koin.dsl.module
import reactor.core.scheduler.Schedulers

val discordClientModule: (String, Long) -> Module = { token: String, intents: Long ->
    module {
        single<GatewayDiscordClient> {
            DiscordClient
                .create(token)
                .gateway()
                .apply {
                    setEnabledIntents(IntentSet.of(intents))
                    setMemberRequestFilter(MemberRequestFilter.none())
                    setSharding(ShardingStrategy.single())
                    setEventDispatcher(
                        EventDispatcher
                            .builder()
                            .eventScheduler(Schedulers.immediate())
                            .build()
                    )
                }
                .login()
                .block()
                ?: throw RuntimeException("Couldn't initialize Discord client")
        }
    }
}
