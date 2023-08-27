package com.tommy.proxy.service

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dto.NodeRegisterRequest
import com.tommy.proxy.dto.NodeRegisterResponse
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
