plugins {
    kotlin("jvm") version "1.5.0"
    application
    id("me.qoomon.git-versioning") version "4.2.0"
    id("io.gitlab.arturbosch.detekt").version("1.17.1")
}

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib"))
    /*implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.4.2")*/
    /*implementation("com.discord4j:discord4j-core") {
        version {
            strictly("3.2.0-SNAPSHOT")
        }
    }*/
    implementation("com.alex1304.botrino:botrino-command:1.0.0-M2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.netty:netty-transport-native-epoll:4.1.60.Final")
    implementation("io.honeybadger:honeybadger-java:2.0.2")
    implementation("com.github.ajalt.clikt:clikt:3.1.0")
    implementation("org.mapdb:mapdb:3.0.8")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.1")

    testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
}

tasks {
    compileKotlin {
        kotlinOptions.languageVersion = "1.5"
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.languageVersion = "1.5"
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

group = "online.senpai"
version = "0.0.0-SNAPSHOT"

gitVersioning.apply(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig> {
    branch(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
        pattern = "main"
        versionFormat = "\${version}"
    })
    branch(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
        pattern = "feature/(?<feature>.+)"
        versionFormat = "\${feature}-SNAPSHOT"
    })
    branch(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
        pattern = "pull/(.+)"
        versionFormat = "\${branch}-SNAPSHOT"
    })
    tag(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
        pattern = "v(?<tagVersion>[0-9].*)"
        versionFormat = "\${tagVersion}"
    })
})

detekt {
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    reports {
        html {
            enabled = true
            destination = file("reports/detekt.html")
        }
    }
}

application {
    mainClass.set("online.senpai.schedbot.MainKt")
}
