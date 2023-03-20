package com.tommy.keyvaluestore.controllers

import com.tommy.keyvaluestore.dtos.KeyValueSaveRequest
import com.tommy.keyvaluestore.dtos.KeyValueSaveResponse
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
    fun save(@RequestBody keyValueSaveRequest: KeyValueSaveRequest): ResponseEntity<KeyValueSaveResponse> {
        val keyValueResponse = keyValueService.put(keyValueSaveRequest)
        return ResponseEntity.ok().body(keyValueResponse)
    }
}
