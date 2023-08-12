package com.tommy.proxy.support

import com.ninjasquad.springmockk.MockkBean
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
class TestMockConfig {

    @TestConfiguration
    class RedisMockConfig(
        @MockkBean private val redissonClient: RedissonClient,
    )
}
