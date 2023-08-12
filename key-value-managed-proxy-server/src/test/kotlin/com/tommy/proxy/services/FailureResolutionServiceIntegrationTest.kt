package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.FailureResolutionRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Disabled("Embedded Redis 설정 필요")
@SpringBootTest
class FailureResolutionServiceIntegrationTest @Autowired constructor(
    private val failureResolutionService: FailureResolutionService,
    private val consistentHashRouter: ConsistentHashRouter,
) {

    private val nodes = listOf(
        "http://127.0.0.1:8081",
        "http://127.0.0.1:8082",
        "http://127.0.0.1:8083",
    )

    @BeforeEach
    fun setUp() {
        nodes.forEach {
            consistentHashRouter.addNode(Instance(it), 10)
        }
    }

    @Test
    fun `sut handle fault node when FaultNodeRequest is given`() {
        // Arrange
        val failureResolutionRequest = FailureResolutionRequest("127.0.0.1:8081", 1)
        val faultNode = Instance("http://${failureResolutionRequest.address}")

        val threadCount = 4
        val executorService = Executors.newFixedThreadPool(32)
        val countDownLatch = CountDownLatch(threadCount)

        assertThat(consistentHashRouter.getOriginHashRingSize()).isEqualTo(30)
        assertThat(consistentHashRouter.getExistingVirtualNodeCount(faultNode)).isEqualTo(10)

        // Act
        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    failureResolutionService.resolveFailureNode(failureResolutionRequest)
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await()

        // Assert
        assertThat(consistentHashRouter.getOriginHashRingSize()).isEqualTo(20)
        assertThat(consistentHashRouter.getExistingVirtualNodeCount(Instance(failureResolutionRequest.address))).isZero
    }
}
