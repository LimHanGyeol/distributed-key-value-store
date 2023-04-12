package com.tommy.keyvaluestore.schedules

import com.tommy.keyvaluestore.dtos.FailureNode
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Component
class FailureDetectionScheduleService(
    private val hostAddress: String,
    private val restTemplate: RestTemplate,
    redisTemplate: StringRedisTemplate,
) {
    private val logger = KotlinLogging.logger { }
    private val valueOperations: ValueOperations<String, String> = redisTemplate.opsForValue()
    private val membership = mutableMapOf<String, Int>()

    @Scheduled(cron = "0/5 * * * * ?")
    fun execute() {
        logger.info { "run failure detection schedule" }
        countMyHeartBeat(hostAddress)
        initMembership()

        val failureNodes = searchFailureNode()
        failureNodes.filter { it.failureNodeCount > 1 }.forEach {
            restTemplate.postForObject("http://localhost:9090/fault-node", it, Unit::class.java)
        }
    }

    private fun countMyHeartBeat(hostAddress: String) {
        val redisKey = "node:$hostAddress"
        val value = valueOperations.get(redisKey)
        if (value == null || value.toInt() == Int.MAX_VALUE) {
            valueOperations.set(redisKey, "0", Duration.ofMinutes(1))
        }

        val heartBeatCount = valueOperations.increment(redisKey)
        valueOperations.set(redisKey, heartBeatCount.toString())
        logger.info { "redisKey: $redisKey, heartBeat Count is $heartBeatCount" }
    }

    private fun initMembership() {
        val remoteNodes = valueOperations.operations.keys("node*") ?: throw RuntimeException()
        logger.info { "search result: $remoteNodes" }
        if (remoteNodes.isNotEmpty()) {
            for (node in remoteNodes) {
                val heartBeat = valueOperations.get(node)!!
                membership[node] = heartBeat.toInt()
            }
        }
    }

    private fun searchFailureNode(): List<FailureNode> {
        var failureNodeCount = 0
        val failureNodes = mutableListOf<FailureNode>()

        val remoteNodes = valueOperations.operations.keys("node*") ?: throw RuntimeException()
        logger.info { "search result: $remoteNodes" }

        if (remoteNodes.isNotEmpty()) {
            for (node in remoteNodes) {
                val memberHeartBeat = membership[node]
                val remoteNodeHeartBeat = valueOperations.get(node) ?: "0"
                if (memberHeartBeat != remoteNodeHeartBeat.toInt()) {
                    failureNodeCount++
                }
                failureNodes.add(FailureNode(node.removePrefix("node:"), failureNodeCount))
                membership[node] = remoteNodeHeartBeat.toInt()
            }
        }
        return failureNodes
    }
}
