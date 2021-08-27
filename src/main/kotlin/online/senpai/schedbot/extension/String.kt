package online.senpai.schedbot.extension

import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField

fun String.parseAsOffsetDateTime(): Mono<LocalDateTime> =
    runCatching {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder().apply {
            appendPattern("[yyyy-M-dd ][yy-M-dd ][M-dd ][dd ]H[:mm][:mm]")
            parseDefaulting(ChronoField.YEAR, ZonedDateTime.now().year.toLong())
            parseDefaulting(ChronoField.MONTH_OF_YEAR, ZonedDateTime.now().monthValue.toLong())
            parseDefaulting(ChronoField.DAY_OF_MONTH, ZonedDateTime.now().dayOfMonth.toLong())
            parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        }
            .toFormatter()
            .withResolverStyle(ResolverStyle.SMART)
        LocalDateTime.parse(this.trim(), formatter) // TODO a better way?
    }
        .fold(
            { Mono.just(it) },
            { Mono.error(it) }
        )
