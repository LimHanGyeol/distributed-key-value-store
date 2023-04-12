package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.FaultNodeRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class FaultNodeServiceIntegrationTest @Autowired constructor(
    private val faultNodeService: FaultNodeService,
    private val consistentHashRouter: ConsistentHashRouter,
) {

    @Test
    fun `sut handle fault node when FaultNodeRequest is given`() {
        // Arrange
        val faultNodeRequest = FaultNodeRequest("http://localhost:8081", 1)

        val threadCount = 4
        val executorService = Executors.newFixedThreadPool(32)
        val countDownLatch = CountDownLatch(threadCount)

        assertThat(consistentHashRouter.getHashRingSize()).isEqualTo(40)
        assertThat(consistentHashRouter.getExistingVirtualNodeCount(Instance(faultNodeRequest.address))).isEqualTo(10)

        // Act
        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    faultNodeService.handleFaultNode(faultNodeRequest)
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await()

        // Assert
        assertThat(consistentHashRouter.getHashRingSize()).isEqualTo(30)
        assertThat(consistentHashRouter.getExistingVirtualNodeCount(Instance(faultNodeRequest.address))).isZero
    }
}
