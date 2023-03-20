package com.tommy.keyvaluestore.services

import com.tommy.keyvaluestore.dtos.KeyValueRequest
import com.tommy.keyvaluestore.dtos.KeyValueResponse
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class KeyValueServiceTest(
    @MockK private val sut: KeyValueService,
) {

    @Test
    @DisplayName("key value 가 입력되면 이를 메모리에 저장한다.")
    fun `sut should return key when key and value is given`() {
        // Arrange
        val keyValueRequest = KeyValueRequest(key = "name", value = "hangyeol")

        every { sut.put(keyValueRequest) } returns KeyValueResponse(keyValueRequest.key)

        // Act
        val actual = sut.put(keyValueRequest)

        // Assert
        assertThat(actual).isInstanceOf(KeyValueResponse::class.java)
        assertThat(actual.key).isEqualTo(keyValueRequest.key)
    }
}
