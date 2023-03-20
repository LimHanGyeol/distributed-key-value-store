package com.tommy.proxy.consistenthashing

import com.tommy.proxy.consistenthashing.node.Instance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ConsistentHashRouterTest {

    @Test
    @DisplayName("안정 해시 초기화")
    fun `init consistent hash router`() {
        // Arrange
        val virtualNodeCount = 10
        val node1 = Instance("127.0.0.1", 80)
        val node2 = Instance("127.0.0.2", 80)
        val node3 = Instance("127.0.0.3", 80)
        val node4 = Instance("127.0.0.4", 80)

        // Act
        val actual = ConsistentHashRouter(
            physicalNodes = listOf(node1, node2, node3, node4),
            virtualNodeCount = virtualNodeCount
        )

        // Assert
        assertThat(actual.getHashRingSize()).isEqualTo(40)
    }

    @Test
    @DisplayName("안정해시 해시링에 노드 추가")
    fun `add node of consistent hash`() {
        // Arrange
        val virtualNodeCount = 10
        val node1 = Instance("127.0.0.1", 80)
        val node2 = Instance("127.0.0.2", 80)
        val node3 = Instance("127.0.0.3", 80)
        val node4 = Instance("127.0.0.4", 80)

        val sut = ConsistentHashRouter(
            physicalNodes = listOf(node1, node2, node3),
            virtualNodeCount = virtualNodeCount
        )

        // Act
        sut.addNode(node4, 10)

        // Assert
        assertThat(sut.getHashRingSize()).isEqualTo(40)
    }

    @Test
    @DisplayName("안정해시 해시링에 노드 제거")
    fun `remove node of consistent hash`() {
        // Arrange
        val virtualNodeCount = 10
        val node1 = Instance("127.0.0.1", 80)
        val node2 = Instance("127.0.0.2", 80)
        val node3 = Instance("127.0.0.3", 80)
        val node4 = Instance("127.0.0.4", 80)

        val sut = ConsistentHashRouter(
            physicalNodes = listOf(node1, node2, node3, node4),
            virtualNodeCount = virtualNodeCount
        )

        // Act
        sut.removeNode(node4)

        // Assert
        assertThat(sut.getHashRingSize()).isEqualTo(30)
    }

    @Test
    @DisplayName("요청으로 인입된 IP 해싱하여 해시링의 가장 근접한 노드로 Routing")
    fun `get route node of consistent hash`() {
        // Arrange
        val virtualNodeCount = 10
        val node1 = Instance("127.0.0.1", 80)
        val node2 = Instance("127.0.0.2", 80)
        val node3 = Instance("127.0.0.3", 80)
        val node4 = Instance("127.0.0.4", 80)

        val requestIP = "192.168.0.4"

        val sut = ConsistentHashRouter(
            physicalNodes = listOf(node1, node2, node3, node4),
            virtualNodeCount = virtualNodeCount
        )

        // Act
        val actual = sut.routeNode(requestIP) ?: throw RuntimeException()

        // Assert
        // hashValue: 38991125
        // 가장 가까운 노드의 hash: 103618170
        // 가장 근접하게 작은 노드의 hash: 34084152
        assertThat(actual.getKey()).isEqualTo("127.0.0.3:80")
    }
}
