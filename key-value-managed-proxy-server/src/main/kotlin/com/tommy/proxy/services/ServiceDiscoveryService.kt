package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.NodeRegisterRequest
import com.tommy.proxy.dtos.NodeRegisterResponse
import org.springframework.stereotype.Service

@Service
class ServiceDiscoveryService(
    private val consistentHashRouter: ConsistentHashRouter,
) {

    fun addNode(nodeRegisterRequest: NodeRegisterRequest): NodeRegisterResponse {
        val physicalNode = Instance(nodeRegisterRequest.address)
        consistentHashRouter.addNode(physicalNode, nodeRegisterRequest.virtualNodeCount)
        return NodeRegisterResponse(
            existsNodeCount = consistentHashRouter.getExistingVirtualNodeCount(physicalNode),
        )
    }
}
