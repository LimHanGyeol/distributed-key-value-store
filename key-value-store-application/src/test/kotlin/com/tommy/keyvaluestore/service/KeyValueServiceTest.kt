package com.tommy.keyvaluestore.service

import com.tommy.keyvaluestore.dto.KeyValueGetResponse
import com.tommy.keyvaluestore.dto.KeyValueSaveRequest
import com.tommy.keyvaluestore.dto.KeyValueSaveResponse
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
        val keyValueSaveRequest = KeyValueSaveRequest(key = "name", value = "hangyeol")

        every { sut.put(keyValueSaveRequest) } returns KeyValueSaveResponse(keyValueSaveRequest.key)

        // Act
        val actual = sut.put(keyValueSaveRequest)

        // Assert
        assertThat(actual).isInstanceOf(KeyValueSaveResponse::class.java)
        assertThat(actual.key).isEqualTo(keyValueSaveRequest.key)
    }

    @Test
    @DisplayName("key 에 해당하는 값이 존재할 경우 해당 값을 응답한다.")
    fun `sut should return value when exist key is given`() {
        // Arrange
        val value = "hangyeol"

        every { sut.get("name") } returns KeyValueGetResponse(value)

        // Act
        val actual = sut.get("name")

        // Assert
        assertThat(actual).isInstanceOf(KeyValueGetResponse::class.java)
        assertThat(actual.value).isEqualTo(value)
    }

    @Test
    @DisplayName("key 에 해당하는 값이 존재하지 않을 경우 null 을 응답한다.")
    fun `sut should return null when not exist key is given`() {
        // Arrange
        every { sut.get("name") } returns KeyValueGetResponse(null)

        // Act
        val actual = sut.get("name")

        // Assert
        assertThat(actual).isInstanceOf(KeyValueGetResponse::class.java)
        assertThat(actual.value).isNull()
    }
}
