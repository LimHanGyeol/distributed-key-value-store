package com.tommy.proxy

import com.tommy.proxy.config.KeyValueRoutesProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProxyApplicationKtTest @Autowired constructor(
    private val keyValueRoutesProperties: KeyValueRoutesProperties,
) {

    @Test
    fun `get key value properties`() {
        println(keyValueRoutesProperties)
        assertThat(keyValueRoutesProperties.nodes).hasSize(4)
    }
}
