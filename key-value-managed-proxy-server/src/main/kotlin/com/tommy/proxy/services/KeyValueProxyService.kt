package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KeyValueProxyService(
    @Value("\${server.port}") private val port: Int,
    private val restTemplate: RestTemplate,
    private val consistentHashRouter: ConsistentHashRouter,
    private val keyValueConsistentService: KeyValueConsistentService,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val hashedKey = consistentHashRouter.doHash(keyValueSaveRequest.key, port)
        val primaryNode = consistentHashRouter.routeNode(hashedKey)

        return try {
            val response =
                restTemplate.postForEntity(primaryNode.getKey(), keyValueSaveRequest, KeyValueSaveResponse::class.java)

            if (response.statusCode.is2xxSuccessful) {
                keyValueConsistentService.consistentKeyValue(keyValueSaveRequest, primaryNode)
            }

            response.body!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }

    fun get(key: String): KeyValueGetResponse {
        val hashedKey = consistentHashRouter.doHash(key, port)
        val instance = consistentHashRouter.routeNode(hashedKey)

        val nodeIp = instance.getKey()

        return try {
            restTemplate.getForObject("$nodeIp?key=$key", KeyValueGetResponse::class.java)!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }
}
