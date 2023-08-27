package com.tommy.proxy.service

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.dto.KeyValueGetResponse
import com.tommy.proxy.dto.KeyValueSaveRequest
import com.tommy.proxy.dto.KeyValueSaveResponse
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
        val hashedKey = consistentHashRouter.doHash(keyValueSaveRequest.key)
        val primaryNode = consistentHashRouter.routeNode(hashedKey)
        logger.info { "hashedKey: $hashedKey, primaryNode: $primaryNode, keyValueSaveRequest: $keyValueSaveRequest" }

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
        val hashedKey = consistentHashRouter.doHash(key)
        val instance = consistentHashRouter.routeNode(hashedKey)
        logger.info { "hashedKey: $hashedKey, instance: $instance, key: $key" }

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
