package online.senpai.schedbot.repository

import online.senpai.schedbot.entity.ScheduledEvent

interface ScheduledRepository : ReactiveCrudRepository<ScheduledEvent, Long>
