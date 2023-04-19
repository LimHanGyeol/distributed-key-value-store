package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class KeyValueConsistentServiceTest(
    @MockK private val restTemplate: RestTemplate,
    @MockK private val hashFunction: HashFunction,
    @MockK private val consistentHashRouter: ConsistentHashRouter,
) {
    @InjectMockKs
    private lateinit var keyValueConsistentService: KeyValueConsistentService

    @Test
    @DisplayName("Secondary Node 에게 KeyValue 에 대한 Post 요청을 전송한다.")
    fun `sut should send post request to secondary node`() {
        // Arrange
        val keyValueSaveRequest = KeyValueSaveRequest("key", "value")
        val primaryNode = Instance("http://localhost:8082")
        val hashedKey = Random.nextInt()

        val secondaryNode = Instance("http://localhost:8081")

        every { hashFunction.doHash(keyValueSaveRequest.key) } returns hashedKey
        every { consistentHashRouter.routeOtherNode(hashedKey, primaryNode) } returns secondaryNode

        every {
            restTemplate.postForEntity(
                "${secondaryNode.getKey()}/put",
                keyValueSaveRequest,
                KeyValueSaveResponse::class.java,
            )
        } returns ResponseEntity.ok().body(KeyValueSaveResponse(keyValueSaveRequest.key))

        // Act
        keyValueConsistentService.consistentPutKeyValue(keyValueSaveRequest, primaryNode)

        // Assert
        verify {
            restTemplate.postForEntity(
                "${secondaryNode.getKey()}/put",
                keyValueSaveRequest,
                KeyValueSaveResponse::class.java,
            )
        }
    }
}
