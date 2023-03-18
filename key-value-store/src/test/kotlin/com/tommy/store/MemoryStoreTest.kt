package com.tommy.store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MemoryStoreTest {

    @BeforeEach
    internal fun setUp() {
        MemoryStore.clear()
    }

    @Test
    @DisplayName("key-value 를 Memory 에 저장한다.")
    fun `put key-value`() {
        // Arrange
        val key = "key"
        val value = "value"

        // Act
        val actual = MemoryStore.put(key, value)

        // Assert
        assertThat(actual).isEqualTo(key)
    }

    @Test
    fun `get key-value`() {
        // Arrange
        MemoryStore.put("key", "value")

        // Act
        val actual = MemoryStore.get("key")

        // Assert
        assertThat(actual).isEqualTo("value")
    }
}
