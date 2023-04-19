package com.tommy.keyvaluestore.batch

import com.tommy.keyvaluestore.dtos.FailureResolutionRequest
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

@Component
class FailureDetectionCronJobExecutor(
    private val hostAddress: String,
    private val restTemplate: RestTemplate,
    private val redisTemplate: StringRedisTemplate,
) {
    private val logger = KotlinLogging.logger { }
    private val valueOperations: ValueOperations<String, String> = redisTemplate.opsForValue()

    private val membership = mutableMapOf<String, Int>()
    private val failureNodes = mutableMapOf<String, Int>()

    @Scheduled(cron = "0/5 * * * * ?")
    fun execute() {
        countMyHeartBeat(hostAddress)
        initMembership()

        failureNodes.filter { it.value == 3 }.forEach {
            try {
                logger.info { ">>> ${it.key} node is failed. failureNodeCount is ${it.value}" }
                val requestBody = FailureResolutionRequest(address = it.key, heatBeatCount = it.value)
                restTemplate.postForObject("http://localhost:8080/fault-node", requestBody, Unit::class.java)
                removeNode(it.key)
            } catch (e: Exception) {
                logger.error { "failure resolution failed message: ${e.message}" }
                removeNode(it.key)
            }
        }
    }

    private fun countMyHeartBeat(hostAddress: String) {
        val redisKey = "node:$hostAddress"
        val value = valueOperations.get(redisKey)
        if (value == null || value.toInt() == Int.MAX_VALUE) {
            valueOperations.set(redisKey, "0")
        }

        val heartBeatCount = valueOperations.increment(redisKey)
        valueOperations.set(redisKey, heartBeatCount.toString())
        redisTemplate.expire(redisKey, 17, TimeUnit.SECONDS)
        logger.info { ">>> redisKey: $redisKey, heartBeat Count is $heartBeatCount" }
    }

    private fun initMembership() {
        val remoteNodes = valueOperations.operations.keys("node*") ?: throw RuntimeException()

        if (remoteNodes.isNotEmpty()) {
            for (node in remoteNodes) {
                val heartBeat = valueOperations.get(node)
                if (heartBeat?.toInt() != membership[node]) {
                    membership[node] = heartBeat!!.toInt()
                } else {
                    val key = node.removePrefix("node:")
                    val count = failureNodes.getOrDefault(key, 0)
                    failureNodes[key] = count + 1
                }
            }
        }
        logger.info { ">>> membership: $membership" }
        logger.info { ">>> failureNodes: $failureNodes" }
        logger.info { ">>> remoteNodes: $remoteNodes" }
    }

    private fun removeNode(failureNode: String) {
        failureNodes.remove(failureNode)
        membership.remove("node:$failureNode")
    }
}
