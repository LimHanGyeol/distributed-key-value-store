package com.tommy.proxy.service

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dto.NodeRegisterRequest
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ServiceDiscoveryServiceTest(
    @MockK private val consistentHashRouter: ConsistentHashRouter,
) {
    @InjectMockKs
    private lateinit var sut: ServiceDiscoveryService

    @Test
    @DisplayName("노드 등록 요청이 주어질 경우 안정해시의 해시링에 노드를 추가한다.")
    fun `sut add node when node register request is given`() {
        // Arrange
        val nodeRegisterRequest = NodeRegisterRequest(address = "127.0.0.1:8080", virtualNodeCount = 10)
        val instance = Instance(nodeRegisterRequest.address)

        every { consistentHashRouter.getExistingVirtualNodeCount(instance) } returns 10
        justRun { consistentHashRouter.addNode(instance, nodeRegisterRequest.virtualNodeCount) }


        // Act
        val actual = sut.addNode(nodeRegisterRequest)

        // Assert
        assertThat(actual.existsNodeCount).isEqualTo(nodeRegisterRequest.virtualNodeCount)

        verify { consistentHashRouter.addNode(instance, nodeRegisterRequest.virtualNodeCount) }
    }
}
