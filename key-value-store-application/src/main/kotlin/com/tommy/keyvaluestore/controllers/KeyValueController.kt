package com.tommy.keyvaluestore.controllers

import com.tommy.keyvaluestore.dtos.KeyValueRequest
import com.tommy.keyvaluestore.dtos.KeyValueResponse
import com.tommy.keyvaluestore.services.KeyValueService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyValueController(
    private val keyValueService: KeyValueService,
) {

    @PostMapping("/")
    fun save(@RequestBody keyValueRequest: KeyValueRequest): ResponseEntity<KeyValueResponse> {
        val keyValueResponse = keyValueService.put(keyValueRequest)
        return ResponseEntity.ok().body(keyValueResponse)
    }
}
