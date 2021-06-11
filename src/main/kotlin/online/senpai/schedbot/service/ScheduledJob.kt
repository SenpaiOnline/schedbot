package online.senpai.schedbot.service

import reactor.core.Disposable
import java.io.Serializable

data class ScheduledJob(
    val disposable: Disposable,
    val data: DataForJobScheduling
) : Serializable
