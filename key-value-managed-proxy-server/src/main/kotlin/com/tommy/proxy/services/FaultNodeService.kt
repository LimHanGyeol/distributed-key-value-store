package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.FaultNodeRequest
import mu.KotlinLogging
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class FaultNodeService(
    private val redissonClient: RedissonClient,
    private val consistentHashRouter: ConsistentHashRouter,
) {
    private val logger = KotlinLogging.logger { }

    fun handleFaultNode(faultNodeRequest: FaultNodeRequest) {
        val lock = redissonClient.getLock(faultNodeRequest.address)

        val available = lock.tryLock(5, 6, TimeUnit.SECONDS)
        try {
            if (!available) {
                logger.info { "잠금 획득 실패!" }
                logger.info { "$faultNodeRequest" }
                return
            }
            val faultNode = Instance(faultNodeRequest.address)
            val existingVirtualNodeCount = consistentHashRouter.getExistingVirtualNodeCount(faultNode)

            if (existingVirtualNodeCount == 0) {
                logger.info { "이미 처리된 잠금입니다." }
                return
            }
            logger.info { "잠금 획득 ${Thread.currentThread()}" }

            consistentHashRouter.removeNode(faultNode)
            return
        } catch (e: Exception) {
            throw RuntimeException()
        } finally {
            lock.unlock()
        }
    }
}
