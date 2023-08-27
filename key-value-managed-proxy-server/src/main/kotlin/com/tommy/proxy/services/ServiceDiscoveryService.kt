package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.NodeRegisterRequest
import com.tommy.proxy.dtos.NodeRegisterResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ServiceDiscoveryService(
    private val consistentHashRouter: ConsistentHashRouter,
) {
    private val logger = KotlinLogging.logger { }

    fun addNode(nodeRegisterRequest: NodeRegisterRequest): NodeRegisterResponse {
        val physicalNode = Instance(nodeRegisterRequest.address)
        consistentHashRouter.addNode(physicalNode, nodeRegisterRequest.virtualNodeCount)

        logger.info { "current total hashing size: ${consistentHashRouter.getOriginHashRingSize()}" }

        return NodeRegisterResponse(existsNodeCount = consistentHashRouter.getExistingVirtualNodeCount(physicalNode))
    }
}
