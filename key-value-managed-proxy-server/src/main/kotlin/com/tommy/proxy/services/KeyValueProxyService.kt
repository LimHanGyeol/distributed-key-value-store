package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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

        val nodeIp = instance.getKey()

        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val responseEntity = restTemplate.postForObject(
                nodeIp,
                HttpEntity(keyValueSaveRequest, headers),
                KeyValueSaveResponse::class.java,
            )

            responseEntity!!
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
