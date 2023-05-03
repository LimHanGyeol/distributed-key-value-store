package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Node
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KeyValueConsistentService(
    private val restTemplate: RestTemplate,
    private val consistentHashRouter: ConsistentHashRouter,
) {
    private val logger = KotlinLogging.logger { }

    @Async
    fun consistentPutKeyValue(keyValueSaveRequest: KeyValueSaveRequest, primaryNode: Node) {
        val secondaryNode = consistentHashRouter.routeOtherNode(keyValueSaveRequest.key, primaryNode)
        logger.info { "keyValueSaveRequest:$keyValueSaveRequest, secondaryNode: $secondaryNode" }

        return try {
            val response = restTemplate.postForEntity(
                "${secondaryNode.getKey()}/put",
                keyValueSaveRequest,
                KeyValueSaveResponse::class.java,
            )
            logger.info { "secondary node consistent result: ${response.statusCode}" }
        } catch (e: Exception) {
            logger.error { e.message }
        }
    }
}
