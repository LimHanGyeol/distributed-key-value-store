package com.tommy.keyvaluestore.schedules

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FailureDetectionScheduleService {
    private val logger = KotlinLogging.logger { }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    fun execute() {
        logger.info { "run failure detection schedule" }
    }
}
