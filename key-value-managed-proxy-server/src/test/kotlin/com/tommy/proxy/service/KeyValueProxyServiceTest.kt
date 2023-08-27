package com.tommy.proxy.service

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dto.KeyValueGetResponse
import com.tommy.proxy.dto.KeyValueSaveRequest
import com.tommy.proxy.dto.KeyValueSaveResponse
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verifyAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@ExtendWith(MockKExtension::class)
class KeyValueProxyServiceTest(
    @MockK private val restTemplate: RestTemplate,
    @MockK private val consistentHashRouter: ConsistentHashRouter,
    @MockK private val keyValueConsistentService: KeyValueConsistentService,
) {

    private val sut: KeyValueProxyService by lazy {
        KeyValueProxyService(
            restTemplate = restTemplate,
            consistentHashRouter = consistentHashRouter,
            keyValueConsistentService = keyValueConsistentService,
        )
    }

    @Test
    @DisplayName("keyValueSaveRequest 가 주어질 경우 Key 를 분산하여 노드에 저장하고 KeyValueSaveResponse를 응답한다.")
    fun `sut should return KeyValueSaveResponse when keyValueSaveRequest is given`() {
        // Arrange
        val request = KeyValueSaveRequest("80a53953-3560-45f0-97f7-384155ff0d06", "value")
        val hashedKey = 714878469L
        val primaryNode = Instance("http://localhost:8082")

        every { consistentHashRouter.doHash(request.key) } returns hashedKey
        every { consistentHashRouter.routeNode(hashedKey) } returns primaryNode
        every {
            restTemplate.postForEntity(eq("${primaryNode.getKey()}/put"), request, KeyValueSaveResponse::class.java)
        } returns ResponseEntity.ok().body(KeyValueSaveResponse(request.key))

        justRun { keyValueConsistentService.consistentPutKeyValue(request, primaryNode) }

        // Act
        val actual = sut.put(request)

        // Assert
        assertThat(actual.key).isEqualTo(request.key)

        verifyAll {
            consistentHashRouter.doHash(request.key)
            consistentHashRouter.routeNode(hashedKey)
            restTemplate.postForEntity(eq("${primaryNode.getKey()}/put"), request, KeyValueSaveResponse::class.java)
            keyValueConsistentService.consistentPutKeyValue(request, primaryNode)
        }
    }

    @Test
    @DisplayName("keyValue 저장이 실패할 때 RuntimeException 을 발생한다.")
    fun `sut should throws RuntimeException when keyValue put is failed`() {
        // Arrange
        val request = KeyValueSaveRequest("80a53953-3560-45f0-97f7-384155ff0d06", "value")
        val hashedKey = 714878469L
        val primaryNode = Instance("http://localhost:8082")

        every { consistentHashRouter.doHash(request.key) } returns hashedKey
        every { consistentHashRouter.routeNode(hashedKey) } returns primaryNode
        every {
            restTemplate.postForEntity(eq("${primaryNode.getKey()}/put"), request, KeyValueSaveResponse::class.java)
        } returns ResponseEntity.internalServerError().build()

        // Act & Assert
        assertThrows<RuntimeException> { sut.put(request) }

        verifyAll {
            consistentHashRouter.doHash(request.key)
            consistentHashRouter.routeNode(hashedKey)
            restTemplate.postForEntity(eq("${primaryNode.getKey()}/put"), request, KeyValueSaveResponse::class.java)
        }
    }

    @Test
    @DisplayName("key 가 주어질 경우 KeyValueGetResponse를 응답한다.")
    fun `sut should return KeyValueGetResponse when key is given`() {
        // Arrange
        val key = "80a53953-3560-45f0-97f7-384155ff0d06"
        val hashedKey = 714878469L
        val instance = Instance("http://localhost:8082")
        val url = "${instance.getKey()}/get?key=$key"

        every { consistentHashRouter.doHash(key) } returns hashedKey
        every { consistentHashRouter.routeNode(hashedKey) } returns instance
        every {
            restTemplate.getForEntity(url, KeyValueGetResponse::class.java)
        } returns ResponseEntity.ok().body(KeyValueGetResponse("value"))

        // Act
        val actual = sut.get(key)

        // Assert
        assertThat(actual.value).isEqualTo("value")

        verifyAll {
            consistentHashRouter.doHash(key)
            consistentHashRouter.routeNode(hashedKey)
            restTemplate.getForEntity(url, KeyValueGetResponse::class.java)
        }
    }
}
