package com.tommy.keyvaluestore.batch

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.web.client.RestTemplate

@ExtendWith(MockKExtension::class)
class FailureDetectionCronJobExecutorTest {

    private val redisTemplate: StringRedisTemplate = mockk(relaxed = true)
    private val restTemplate: RestTemplate = mockk()
    private val sut = FailureDetectionCronJobExecutor("127.0.0.1:8080", restTemplate, redisTemplate)

    @Test
    @DisplayName("정해진 크론식 시간주기로 노드의 HeartBeat 를 계산하여 장애 감지를 한다.")
    fun `sut should update heartBeat count when at fixed times`() {
        // Arrange
        val redisKey = "node:127.0.0.1:8080"
        val heartBeatCount = 1L

        val valueOperations: ValueOperations<String, String> = redisTemplate.opsForValue()

        every { valueOperations.get(redisKey) } returns "0"
        every { valueOperations.increment(any()) } returns heartBeatCount
        every { valueOperations.set(redisKey, heartBeatCount.toString()) } returns Unit

        every { valueOperations.operations.keys("node*") } returns setOf(redisKey)
        every { redisTemplate.opsForValue().get(redisKey) } returns heartBeatCount.toString()

        // Act
        sut.execute()

        // Assert
        verifyAll {
            valueOperations.get("node:127.0.0.1:8080")
            valueOperations.increment("node:127.0.0.1:8080")
            valueOperations.set("node:127.0.0.1:8080", any())
            valueOperations.operations.keys("node*")
        }
    }
}
