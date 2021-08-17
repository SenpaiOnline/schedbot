package online.senpai.schedbot.extension

import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField

fun String.parseAsOffsetDateTime(): Mono<OffsetDateTime> =
    runCatching {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder().apply {
            appendPattern("[yyyy-M-dd ][yy-M-dd ][M-dd ][dd ]H[:mm][:mm X][ X]")
            parseDefaulting(ChronoField.YEAR, ZonedDateTime.now().year.toLong())
            parseDefaulting(ChronoField.MONTH_OF_YEAR, ZonedDateTime.now().monthValue.toLong())
            parseDefaulting(ChronoField.DAY_OF_MONTH, ZonedDateTime.now().dayOfMonth.toLong())
            parseDefaulting(ChronoField.OFFSET_SECONDS, ZoneOffset.UTC.totalSeconds.toLong())
            parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        }
            .toFormatter()
            .withResolverStyle(ResolverStyle.SMART)
        OffsetDateTime.parse(this.trim(), formatter) // TODO a better way?
    }
        .fold(
            { Mono.just(it) },
            { Mono.error(it) }
        )
