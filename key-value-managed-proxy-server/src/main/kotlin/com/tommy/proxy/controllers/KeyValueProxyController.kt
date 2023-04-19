package com.tommy.proxy.controllers

import com.tommy.proxy.dtos.FailureResolutionRequest
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import com.tommy.proxy.services.FailureResolutionService
import com.tommy.proxy.services.KeyValueProxyService
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
