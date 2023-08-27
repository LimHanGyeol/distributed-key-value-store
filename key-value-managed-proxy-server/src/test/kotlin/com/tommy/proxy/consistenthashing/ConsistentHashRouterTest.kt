package com.tommy.proxy.consistenthashing

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
        "http://localhost:8084",
    )

    private val sut: ConsistentHashRouter by lazy {
        ConsistentHashRouter(hashFunction = MurmurHash3())
    }

    @BeforeEach
    fun setUp() {
        nodes.forEach {
            sut.addNode(Instance(it), 10)
        }
    }

    @Test
    @DisplayName("안정 해시 초기화")
    fun `init consistent hash router`() {
        // Arrange

        // Act
        val actual = sut

        // Assert
        assertThat(actual.getOriginHashRingSize()).isEqualTo(40)
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
        assertThat(sut.getOriginHashRingSize()).isEqualTo(50)
    }

    @Test
    @DisplayName("안정해시 해시링에 노드 제거")
    fun `remove node of consistent hash`() {
        // Arrange
        val targetPhysicalNode = Instance("http://localhost:8083")

        // Act
        sut.removeNode(targetPhysicalNode)

        // Assert
        assertThat(sut.getOriginHashRingSize()).isEqualTo(30)
    }

    @Test
    @DisplayName("요청으로 인입된 Key 를 해싱하여 해시링의 가장 근접한 노드로 Routing 한다.")
    fun `get route node of consistent hash`() {
        // Arrange
        val key = "80a53953-3560-45f0-97f7-384155ff0d06"
        val hashedKey = sut.doHash(key)

        // Act
        val actual = sut.routeNode(hashedKey)

        // Assert
        // hashedKey: -4797992637988812037
        // 가장 가까운 노드의 hash 값: -4421527676906511933
        // 가장 가까운 노드 정보: VirtualNode(physicalNode=http://localhost:8082, virtualIndex=2)
        assertThat(actual.getKey()).isEqualTo("http://localhost:8082")
    }

    @Test
    @DisplayName("요청으로 인입된 Key 를 해싱하여 Primary 노드 외 다른 노드에게 Routing 한다.")
    fun `get route other node of consistent hash`() {
        // Arrange
        val key = "80a53953-3560-45f0-97f7-384155ff0d06"

        val primaryNode = Instance("http://localhost:8082")

        // Act
        val hashedKey = sut.doHash(key)
        val actual = sut.routeOtherNode(hashedKey, primaryNode)

        // Assert
        // hashedKey: -4797992637988812037
        // 가장 가까운 노드의 hash 값: -4421527676906511933 (http://localhost:8082)
        // 두 번째 가까운 노드의 hash 값: -4312220692094205533
        // 두 번째 가까운 노드의 정보: VirtualNode(physicalNode=http://localhost:8083, virtualIndex=7)
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
        assertThat(sut.getOriginHashRingSize()).isEqualTo(30)
        assertThat(sut.getReplicaHashRingSize()).isEqualTo(40)
    }
}
