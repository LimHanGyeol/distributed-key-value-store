package com.tommy.keyvaluestore.controller

import com.tommy.keyvaluestore.dto.KeyValueGetResponse
import com.tommy.keyvaluestore.dto.KeyValueSaveRequest
import com.tommy.keyvaluestore.dto.KeyValueSaveResponse
import com.tommy.keyvaluestore.service.KeyValueService
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
