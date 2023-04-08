package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.hash.HashFunction
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
    private val hashFunction: HashFunction,
    private val consistentHashRouter: ConsistentHashRouter,
) {
    private val logger = KotlinLogging.logger { }

    @Async
    fun consistentKeyValue(keyValueSaveRequest: KeyValueSaveRequest, primaryNode: Node) {
        val hashedKey = hashFunction.doHash(keyValueSaveRequest.key)
        val secondaryNode = consistentHashRouter.routeOtherNode(hashedKey, primaryNode)

        return try {
            val response = restTemplate.postForEntity(
                secondaryNode.getKey(),
                keyValueSaveRequest,
                KeyValueSaveResponse::class.java,
            )
            logger.info { "secondary node consistent result: ${response.statusCode}" }
        } catch (e: Exception) {
            logger.error { e.message }
        }
    }
}
