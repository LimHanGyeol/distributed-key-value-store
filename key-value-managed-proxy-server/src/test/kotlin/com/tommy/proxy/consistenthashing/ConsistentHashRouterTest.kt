package com.tommy.proxy.consistenthashing

import com.tommy.proxy.config.KeyValueRoutesProperties
import com.tommy.proxy.consistenthashing.hash.MurmurHash3
import com.tommy.proxy.consistenthashing.node.Instance
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ConsistentHashRouterTest {

    private val nodes = listOf(
        "http://localhost:8081",
        "http://localhost:8082",
        "http://localhost:8083",
    )

    private val sut: ConsistentHashRouter by lazy {
        ConsistentHashRouter(
            virtualNodeCount = 10,
            keyValueRoutesProperties = KeyValueRoutesProperties(nodes = nodes),
            hashFunction = MurmurHash3(),
        )
    }

    @BeforeEach
    fun setUp() {
        sut.initNodes()
    }

    @Test
    @DisplayName("안정 해시 초기화")
    fun `init consistent hash router`() {
        // Arrange

        // Act
        val actual = sut

        // Assert
        assertThat(actual.getOriginHashRingSize()).isEqualTo(30)
    }

    @Test
    @DisplayName("안정해시 해시링에 노드 추가")
    fun `add node of consistent hash`() {
        // Arrange
        val virtualNodeCount = 10
        val targetPhysicalNode = Instance("http://localhost:8085")

        // Act
        sut.addNode(targetPhysicalNode, virtualNodeCount)

        // Assert
        assertThat(sut.getOriginHashRingSize()).isEqualTo(40)
    }

    @Test
    @DisplayName("안정해시 해시링에 노드 제거")
    fun `remove node of consistent hash`() {
        // Arrange
        val targetPhysicalNode = Instance("http://localhost:8083")

        // Act
        sut.removeNode(targetPhysicalNode)

        // Assert
        assertThat(sut.getOriginHashRingSize()).isEqualTo(20)
    }

    @Test
    @DisplayName("요청으로 인입된 Key 를 해싱하여 해시링의 가장 근접한 노드로 Routing 한다.")
    fun `get route node of consistent hash`() {
        // Arrange
        val key = "80a53953-3560-45f0-97f7-384155ff0d06"

        // Act
        val actual = sut.routeNode(key)

        // Assert
        // hashedKey: 714878469
        // 해당하는 노드 정보: VirtualNode(physicalNode=http://localhost:8082, virtualIndex=6)
        assertThat(actual.getKey()).isEqualTo("http://localhost:8082")
    }

    @Test
    @DisplayName("요청으로 인입된 Key 를 해싱하여 Primary 노드 외 다른 노드에게 Routing 한다.")
    fun `get route other node of consistent hash`() {
        // Arrange
        val key = "80a53953-3560-45f0-97f7-384155ff0d06"

        val primaryNode = Instance("http://localhost:8082")

        // Act
        val actual = sut.routeOtherNode(key, primaryNode)

        // Assert
        // hashedKey: 714878469
        // 해당하는 Secondary 노드의 정보: VirtualNode(physicalNode=http://localhost:8083, virtualIndex=6)
        assertThat(actual.getKey()).isEqualTo("http://localhost:8083")
    }

    @Test
    @DisplayName("원본 해시링을 복제하여 데이터를 동기화한다.")
    fun `replicate hash ring`() {
        // Arrange

        // Act
        sut.replicateHashRing()
        sut.removeNode(Instance("http://localhost:8081"))

        // Assert
        assertThat(sut.getOriginHashRingSize()).isEqualTo(20)
        assertThat(sut.getReplicaHashRingSize()).isEqualTo(30)
    }
}
