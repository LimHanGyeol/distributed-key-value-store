package com.tommy.proxy

import com.tommy.proxy.support.TestMockConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestMockConfig::class)
@SpringBootTest
class ProxyApplicationKtTest @Autowired constructor(
) {

    @Test
    fun `application_context`() {
    }
}
