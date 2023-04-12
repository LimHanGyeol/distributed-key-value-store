package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.FaultNodeRequest
import com.tommy.proxy.exceptions.AlreadyProcessedLockException
import com.tommy.proxy.exceptions.LockAcquisitionFailedException
import com.tommy.proxy.exceptions.LockProcessFailedException
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
                throw LockAcquisitionFailedException("잠금 획득 실패($faultNodeRequest)")
            }
            val faultNode = Instance(faultNodeRequest.address)
            val existingVirtualNodeCount = consistentHashRouter.getExistingVirtualNodeCount(faultNode)

            if (existingVirtualNodeCount == 0) {
                throw AlreadyProcessedLockException("이미 처리된 요청($faultNodeRequest)")
            }

            consistentHashRouter.removeNode(faultNode)
            return
        } catch (e: Exception) {
            logger.error { e }
            throw LockProcessFailedException(e.message!!)
        } finally {
            lock.unlock()
        }
    }
}
