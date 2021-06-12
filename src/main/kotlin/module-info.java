import botrino.api.annotation.BotModule;

@BotModule
open module online.senpai.schedbot {
    requires botrino.api;
    requires botrino.command;
    requires kotlin.stdlib;
    requires honeybadger.java;
    requires jdk.unsupported;
    requires mapdb;
    requires com.github.ajalt.clikt;
    requires koin.logger.slf4j;
    requires koin.core.jvm;
}
