package online.senpai.schedbot.service

import com.github.alex1304.rdi.finder.annotation.RdiFactory
import com.github.alex1304.rdi.finder.annotation.RdiService
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import org.mapdb.*
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val PATH_TO_DB = "./db/scheduler.db"

@RdiService
class SchedulingService @RdiFactory constructor(private val discordClient: GatewayDiscordClient) {
    private val dbMaker: DB
    private val db: HTreeMap<UUID, DataForJobScheduling>
    private val timerTaskScheduler: Scheduler
        get() = discordClient.coreResources.reactorResources.timerTaskScheduler
    private val _runningJobs: MutableMap<UUID, ScheduledJob> =
        Collections.synchronizedMap(mutableMapOf<UUID, ScheduledJob>())
    val runningJobs: MutableSet<MutableMap.MutableEntry<UUID, ScheduledJob>>
        get() = _runningJobs.entries

    init {
        dbMaker = DBMaker // TODO service?
            .fileDB(PATH_TO_DB)
            .allocateStartSize(1_048_576L)
            .fileMmapEnableIfSupported()
            .fileMmapPreclearDisable()
            .transactionEnable()
            .executorEnable() // Asynchronous write
            .closeOnJvmShutdown()
            .make()
        db = dbMaker
            .hashMap("scheduler")
            .keySerializer(Serializer.UUID)
            .valueSerializer(DataForJobSchedulingSerializer())
            .counterEnable()
            .createOrOpen()
        loadPreviouslyScheduledJobs()
        updateStatus()
    }

    private fun loadPreviouslyScheduledJobs() {
        db.forEach { (uuid: UUID, data: DataForJobScheduling) ->
            scheduleNewJob(data, uuid).subscribe() // TODO
        }
    }

    private fun updateStatus() {
        discordClient
            /*.updatePresence(online(ClientActivity.competing("Scheduled jobs: ${_runningJobs.size}")))*/
            .updatePresence(Presence.online(Activity.competing("Scheduled jobs: ${_runningJobs.size}")))
            .subscribe()
    }

    fun scheduleNewJob(data: DataForJobScheduling, uuid: UUID = UUID.randomUUID()): Mono<Void> =
        Mono
            .defer {
                Mono.fromCallable {
                    Mono
                        .just(data.messageChannel)
                        .map(Snowflake::of)
                        .flatMap { messageChannelId: Snowflake ->
                            discordClient
                                .getChannelById(messageChannelId)
                                .cast(MessageChannel::class.java)
                                .delayElement(
                                    Duration.between(OffsetDateTime.now(), data.delayUntil),
                                    timerTaskScheduler
                                )
                                .flatMap { messageChannel: MessageChannel ->
                                    messageChannel
                                        .createMessage(data.rawMessage)
                                        .doFirst {
                                            if (!db.containsKey(uuid)) {
                                                db[uuid] = data
                                                dbMaker.commit()
                                            }
                                        }
                                        .doFinally {
                                            db.remove(uuid)
                                            dbMaker.commit()
                                            _runningJobs.remove(uuid) // TODO
                                            updateStatus()
                                        }
                                }
                        }
                        .then()
                        .subscribe()
                }
                .doOnNext { disposable: Disposable ->
                    _runningJobs[uuid] = ScheduledJob(disposable, data) // TODO
                    updateStatus()
                }
                .then()
            }
            .subscribeOn(Schedulers.boundedElastic())
}
