package com.tommy.proxy.controllers

import com.tommy.proxy.dtos.NodeRegisterRequest
import com.tommy.proxy.dtos.NodeRegisterResponse
import com.tommy.proxy.services.ServiceDiscoveryService
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
