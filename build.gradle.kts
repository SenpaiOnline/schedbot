plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("kapt") version "1.5.30"
    application
    id("me.qoomon.git-versioning") version "4.2.0"
    id("io.gitlab.arturbosch.detekt").version("1.18.0")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.discord4j:discord4j-core:3.2.0-RC3")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.4")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("io.honeybadger:honeybadger-java:2.0.2")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("io.r2dbc:r2dbc-spi:0.9.0.M2")
    implementation("io.r2dbc:r2dbc-pool:0.9.0.M2")
    implementation("io.r2dbc:r2dbc-h2:0.8.4.RELEASE")
    implementation("io.insert-koin:koin-core:3.1.2")
    implementation("io.insert-koin:koin-logger-slf4j:3.1.2")
    implementation("online.senpai:newbase60:1.0.1")
    implementation("org.mapstruct:mapstruct:1.4.2.Final")
    kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.1")
    testImplementation("io.insert-koin:koin-test:3.1.2")
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

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
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
    mainClass.set("online.senpai.schedbot.LauncherKt")
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
