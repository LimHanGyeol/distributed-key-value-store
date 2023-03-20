package com.tommy.proxy.controllers

import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import com.tommy.proxy.services.KeyValueProxyService
import com.tommy.proxy.utils.IpUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyValueProxyController(
    private val keyValueProxyService: KeyValueProxyService,
) {

    @PostMapping("/")
    fun save(
        @RequestBody keyValueSaveRequest: KeyValueSaveRequest,
        request: HttpServletRequest,
    ): KeyValueSaveResponse {
        val clientIp = IpUtil.getClientIp(request)
        return keyValueProxyService.put(clientIp, keyValueSaveRequest)
    }

    @GetMapping
    fun get(
        @RequestParam key: String,
        request: HttpServletRequest,
    ): KeyValueGetResponse {
        val clientIp = IpUtil.getClientIp(request)
        return keyValueProxyService.get(clientIp, key)
    }
}
