package com.tommy.proxy.consistenthashing.hash

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MurmurHash3Test {

    private val sut = MurmurHash3()

    @Test
    @DisplayName("MurmurHash3 을 이용하여 key 해싱")
    fun `do hash by MurmurHash3`() {
        // Arrange
        val key = "127.0.0.1:8080-0"
        val seed = 0

        // Act
        val actual = sut.doHash(key, seed)

        // Assert
        assertThat(actual).isEqualTo(184398511)
    }
}
