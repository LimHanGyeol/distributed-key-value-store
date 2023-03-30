package com.tommy.keyvaluestore.schedules

import java.net.InetAddress
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FailureDetectionScheduleService(
    @Value("\${server.port}")
    val port: Int,
    redisTemplate: StringRedisTemplate,
) {
    private val logger = KotlinLogging.logger { }
    private val valueOperations: ValueOperations<String, String> = redisTemplate.opsForValue()

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    fun execute() {
        logger.info { "run failure detection schedule" }
        val host = InetAddress.getLocalHost().hostAddress
        countHeartBeat(host)
    }

    private fun countHeartBeat(host: String) {
        val redisKey = "$host:$port"
        val value = valueOperations.get(redisKey)
        if (value == null) {
            valueOperations.set(redisKey, "0")
        }
        val heartBeatCount = valueOperations.increment(redisKey)
        valueOperations.set(redisKey, heartBeatCount.toString())
        logger.info { "redisKey: $redisKey, heartBeat Count is $heartBeatCount" }
    }
}
