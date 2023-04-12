package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.FaultNodeRequest
import com.tommy.proxy.exceptions.LockProcessFailedException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class FaultNodeServiceTest(
    @MockK private val redissonClient: RedissonClient,
    @MockK private val lock: RLock,
    @MockK private val consistentHashRouter: ConsistentHashRouter,
) {
    @InjectMockKs
    private lateinit var sut: FaultNodeService

    @Test
    @DisplayName("실패한 노드 정보가 주어질 경우 해당 노드를 안정해시에서 제거한다.")
    fun `sut handle fault node when FaultNodeRequest is given`() {
        // Arrange
        val faultNodeRequest = FaultNodeRequest("http://localhost:8081", 1)
        val faultNode = Instance(faultNodeRequest.address)

        every { redissonClient.getLock(faultNodeRequest.address) } returns lock
        every { lock.tryLock(5, 6, TimeUnit.SECONDS) } returns true
        every { consistentHashRouter.getExistingVirtualNodeCount(faultNode) } returns 1
        justRun { consistentHashRouter.replicateHashRing() }
        justRun { consistentHashRouter.removeNode(faultNode) }
        justRun { lock.unlock() }

        // Act
        sut.handleFaultNode(faultNodeRequest)

        // Assert
        verifyAll {
            redissonClient.getLock(faultNodeRequest.address)
            lock.tryLock(5, 6, TimeUnit.SECONDS)
            consistentHashRouter.getExistingVirtualNodeCount(faultNode)
            consistentHashRouter.replicateHashRing()
            consistentHashRouter.removeNode(faultNode)
            lock.unlock()
        }
    }

    @Test
    @DisplayName("이미 장애처리가 된 노드일 경우 LockProcessFailedException 을 발생한다.")
    fun `sut throw an exception when already handled fault node`() {
        // Arrange
        val faultNodeRequest = FaultNodeRequest("http://localhost:8081", 1)
        val faultNode = Instance(faultNodeRequest.address)

        every { redissonClient.getLock(faultNodeRequest.address) } returns lock
        every { lock.tryLock(5, 6, TimeUnit.SECONDS) } returns true
        every { consistentHashRouter.getExistingVirtualNodeCount(faultNode) } returns 0
        justRun { lock.unlock() }

        // Act & Assert
        assertThrows<LockProcessFailedException> { sut.handleFaultNode(faultNodeRequest) }
        verify { redissonClient.getLock(faultNodeRequest.address) }
        verify { lock.tryLock(5, 6, TimeUnit.SECONDS) }
        verify { consistentHashRouter.getExistingVirtualNodeCount(faultNode) }
        verify(exactly = 0) { consistentHashRouter.removeNode(faultNode) }
        verify { lock.unlock() }
    }

    @Test
    @DisplayName("잠금을 획득하지 못했을 경우 LockProcessFailedException 을 발생한다.")
    fun `sut throws an exception when lock not acquired`() {
        // Arrange
        val faultNodeRequest = FaultNodeRequest("http://localhost:8081", 1)
        val faultNode = Instance(faultNodeRequest.address)

        every { redissonClient.getLock(faultNodeRequest.address) } returns lock
        every { lock.tryLock(5, 6, TimeUnit.SECONDS) } returns false
        justRun { lock.unlock() }

        // Act & Assert
        assertThrows<LockProcessFailedException> { sut.handleFaultNode(faultNodeRequest) }
        verify { redissonClient.getLock(faultNodeRequest.address) }
        verify { lock.tryLock(5, 6, TimeUnit.SECONDS) }
        verify(exactly = 0) { consistentHashRouter.getExistingVirtualNodeCount(faultNode) }
        verify(exactly = 0) { consistentHashRouter.removeNode(faultNode) }
        verify { lock.unlock() }
    }
}
