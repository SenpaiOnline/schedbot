package online.senpai.schedbot.handler

import botrino.api.annotation.Primary
import botrino.api.config.ConfigContainer
import botrino.api.config.LoginHandler
import botrino.api.config.`object`.BotConfig
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Presence
import discord4j.core.shard.MemberRequestFilter
import discord4j.gateway.intent.IntentSet
import reactor.core.publisher.Mono


@Suppress("unused")
@Primary
class LoginHandlerImpl : LoginHandler {
    override fun login(configContainer: ConfigContainer): Mono<GatewayDiscordClient> {
        val config = configContainer.get(BotConfig::class.java)
        val discordClient = DiscordClient.create(config.token())
        return discordClient
            .gateway()
            /*.setInitialPresence {
                config
                    .presence()
                    .map(BotConfig.StatusConfig::toStatusUpdate)
                    .orElseGet(Presence::online)
            }*/
            .setEnabledIntents(
                config
                    .enabledIntents()
                    .stream() // TODO
                    .boxed()
                    .map(IntentSet::of)
                    .findAny()
                    .orElseGet(IntentSet::nonPrivileged)
            )
            .setMemberRequestFilter(MemberRequestFilter.none())
            .login()
            .single()
    }
}
