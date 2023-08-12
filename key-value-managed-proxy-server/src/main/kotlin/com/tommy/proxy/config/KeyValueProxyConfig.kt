package com.tommy.proxy.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class KeyValueProxyConfig {

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate? {
        return restTemplateBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setConnectTimeout(Duration.ofMillis(5000)).setReadTimeout(Duration.ofMillis(5000))
            .additionalMessageConverters(StringHttpMessageConverter(Charsets.UTF_8)).build()
    }

    @Bean
    fun taskExecutor(): Executor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 5
        taskExecutor.maxPoolSize = 10
        taskExecutor.queueCapacity = 25
        return taskExecutor
    }
}
