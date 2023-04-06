package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KeyValueProxyService(
    private val restTemplate: RestTemplate,
    private val hashFunction: HashFunction,
    private val consistentHashRouter: ConsistentHashRouter,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val hashedKey = hashFunction.doHash(keyValueSaveRequest.key)
        val instance = consistentHashRouter.routeNode(hashedKey)

        return try {
            val responseEntity =
                restTemplate.postForEntity(instance.getKey(), keyValueSaveRequest, KeyValueSaveResponse::class.java)
            responseEntity.body!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }

    fun get(key: String): KeyValueGetResponse {
        val hashedKey = hashFunction.doHash(key)
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
