package com.tommy.proxy.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "key-value.routes")
data class KeyValueRoutesProperties(
    val nodes: List<String>,
)
