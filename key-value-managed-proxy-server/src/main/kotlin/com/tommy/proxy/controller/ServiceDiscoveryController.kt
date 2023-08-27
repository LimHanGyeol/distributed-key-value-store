package com.tommy.proxy.controller

import com.tommy.proxy.dto.NodeRegisterRequest
import com.tommy.proxy.dto.NodeRegisterResponse
import com.tommy.proxy.service.ServiceDiscoveryService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ServiceDiscoveryController(
    private val serviceDiscoveryService: ServiceDiscoveryService,
) {

    @PostMapping("/node")
    fun registerService(@RequestBody nodeRegisterRequest: NodeRegisterRequest): NodeRegisterResponse {
        return serviceDiscoveryService.addNode(nodeRegisterRequest)
    }
}
