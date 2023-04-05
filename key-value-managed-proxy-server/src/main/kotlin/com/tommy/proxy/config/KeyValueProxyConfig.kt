package com.tommy.proxy.config

import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(KeyValueRoutesProperties::class)
class KeyValueProxyConfig {

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate? {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .additionalMessageConverters(StringHttpMessageConverter(Charsets.UTF_8))
            .build()
    }
}
