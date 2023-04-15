package com.tommy.proxy.controllers

import com.tommy.proxy.dtos.FaultNodeRequest
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import com.tommy.proxy.services.FaultNodeService
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
    private val faultNodeService: FaultNodeService,
) {
    private val logger = KotlinLogging.logger { }

    @PostMapping("/")
    fun save(
        @RequestBody keyValueSaveRequest: KeyValueSaveRequest,
    ): KeyValueSaveResponse {
        logger.info { "keyValueSaveRequest: $keyValueSaveRequest" }
        return keyValueProxyService.put(keyValueSaveRequest)
    }

    @GetMapping
    fun get(
        @RequestParam key: String,
    ): KeyValueGetResponse {
        logger.info { "key: $key" }
        return keyValueProxyService.get(key)
    }

    @PostMapping("/fault-node")
    fun handlingFaultNode(@RequestBody faultNodeRequest: FaultNodeRequest) {
        faultNodeService.handleFaultNode(faultNodeRequest)
    }
}
