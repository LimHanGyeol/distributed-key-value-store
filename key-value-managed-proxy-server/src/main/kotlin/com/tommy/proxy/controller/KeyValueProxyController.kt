package com.tommy.proxy.controller

import com.tommy.proxy.dto.FailureResolutionRequest
import com.tommy.proxy.dto.KeyValueGetResponse
import com.tommy.proxy.dto.KeyValueSaveRequest
import com.tommy.proxy.dto.KeyValueSaveResponse
import com.tommy.proxy.service.FailureResolutionService
import com.tommy.proxy.service.KeyValueProxyService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyValueProxyController(
    private val keyValueProxyService: KeyValueProxyService,
    private val failureResolutionService: FailureResolutionService,
) {
    private val logger = KotlinLogging.logger { }

    @PostMapping("/put")
    fun save(
        @RequestBody keyValueSaveRequest: KeyValueSaveRequest,
    ): KeyValueSaveResponse {
        logger.info { "keyValueSaveRequest: $keyValueSaveRequest" }
        return keyValueProxyService.put(keyValueSaveRequest)
    }

    @GetMapping("/get")
    fun get(
        @RequestParam key: String,
    ): KeyValueGetResponse {
        logger.info { "key: $key" }
        return keyValueProxyService.get(key)
    }

    @PostMapping("/failure-resolution")
    fun resolveFailureNode(@RequestBody failureResolutionRequest: FailureResolutionRequest) {
        failureResolutionService.resolveFailureNode(failureResolutionRequest)
    }
}
