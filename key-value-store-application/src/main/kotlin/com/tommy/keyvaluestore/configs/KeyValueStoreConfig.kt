package com.tommy.keyvaluestore.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate
import java.net.InetAddress
import java.time.Duration

@Configuration
@EnableScheduling
class KeyValueStoreConfig {

    @Bean
    fun hostAddress(@Value("\${server.port}") port: Int): String {
        val hostIp = InetAddress.getLocalHost().hostAddress
        return "$hostIp:$port"
    }

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate? {
        return restTemplateBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setConnectTimeout(Duration.ofMillis(5000)).setReadTimeout(Duration.ofMillis(5000))
            .additionalMessageConverters(StringHttpMessageConverter(Charsets.UTF_8)).build()
    }
}
