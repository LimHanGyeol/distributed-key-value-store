package com.tommy.keyvaluestore.controllers

import com.tommy.keyvaluestore.dtos.KeyValueGetResponse
import com.tommy.keyvaluestore.dtos.KeyValueSaveRequest
import com.tommy.keyvaluestore.dtos.KeyValueSaveResponse
import com.tommy.keyvaluestore.services.KeyValueService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyValueController(
    private val keyValueService: KeyValueService,
) {
    private val logger = KotlinLogging.logger { }

    @PostMapping("/put")
    fun save(@RequestBody keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        logger.info { "request: $keyValueSaveRequest" }
        return keyValueService.put(keyValueSaveRequest)
    }

    @GetMapping("/get")
    fun get(@RequestParam key: String): KeyValueGetResponse {
        logger.info { "key: $key" }
        return keyValueService.get(key)
    }
}
