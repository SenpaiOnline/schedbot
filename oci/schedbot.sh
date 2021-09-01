#!/usr/bin/env bash
set -xe

readonly BUILD_IMAGE=library/gradle:7.2.0-jdk11
readonly RUNTIME_IMAGE=bellsoft/liberica-openjre-alpine:11.0.12-7
readonly USER=schedbot

readonly BUILDCTR=$(buildah from docker.io/$BUILD_IMAGE)
buildah config --workingdir "/home/gradle/project" "$BUILDCTR"
buildah copy --chown=gradle:gradle "$BUILDCTR" build.gradle.kts gradle.properties settings.gradle.kts .
buildah copy --chown=gradle:gradle "$BUILDCTR" src/main src/main
buildah run "$BUILDCTR" -- gradle shadowJar --no-daemon

readonly RUNTIMECTR=$(buildah from docker.io/$RUNTIME_IMAGE)
buildah run "$RUNTIMECTR" -- adduser --disabled-password --no-create-home "$USER"
buildah config --workingdir "/app" --volume "/app/data" --user "$USER" --entrypoint '["java", "-jar", "schedbot.jar"]' --cmd "" "$RUNTIMECTR"
buildah copy --chown="$USER" "$RUNTIMECTR" "$(buildah mount "$BUILDCTR")/home/gradle/project/build/libs/*.jar" "schedbot.jar"
buildah unmount "$BUILDCTR"
buildah commit "$RUNTIMECTR" "schedbot:${1:-latest}"
