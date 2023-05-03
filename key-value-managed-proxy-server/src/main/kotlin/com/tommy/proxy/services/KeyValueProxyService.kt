package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KeyValueProxyService(
    private val restTemplate: RestTemplate,
    private val consistentHashRouter: ConsistentHashRouter,
    private val keyValueConsistentService: KeyValueConsistentService,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val primaryNode = consistentHashRouter.routeNode(keyValueSaveRequest.key)
        logger.info { "keyValueSaveRequest: $keyValueSaveRequest, primaryNode: $primaryNode, " }

        return try {
            val response = restTemplate.postForEntity(
                "${primaryNode.getKey()}/put",
                keyValueSaveRequest,
                KeyValueSaveResponse::class.java,
            )

            if (response.statusCode.is2xxSuccessful) {
                keyValueConsistentService.consistentPutKeyValue(keyValueSaveRequest, primaryNode)
            }

            response.body!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }

    fun get(key: String): KeyValueGetResponse {
        val instance = consistentHashRouter.routeNode(key)
        logger.info { "key: $key, instance: $instance, " }

        return try {
            val response =
                restTemplate.getForEntity("${instance.getKey()}/get?key=$key", KeyValueGetResponse::class.java)

            response.body!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }
}
