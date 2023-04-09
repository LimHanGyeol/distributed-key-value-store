package com.tommy.keyvaluestore.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.net.InetAddress

@Configuration
@EnableScheduling
class KeyValueStoreConfig {

    @Bean
    fun hostAddress(@Value("\${server.port}") port: Int): String {
        val hostIp = InetAddress.getLocalHost().hostAddress
        return "$hostIp:$port"
    }
}
