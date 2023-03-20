package com.tommy.proxy.consistent_hashing.hash

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MD5HashTest {

    private val sut = MD5Hash()

    @Test
    @DisplayName("MD5 를 이용하여 key 해싱")
    fun `do hash by MD5`() {
        // Arrange
        val key = "127.0.0.1:8080-0"

        // Act
        val actual = sut.doHash(key)

        // Assert
        assertThat(actual).isEqualTo(1007826611L)
    }
}
