package com.tommy.proxy.services

import com.tommy.proxy.config.KeyValueRoutesProperties
import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.hash.MurmurHash3
import com.tommy.proxy.consistenthashing.node.Instance
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
    private val keyValueRoutesProperties: KeyValueRoutesProperties,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val nodes = keyValueRoutesProperties.nodes.map { Instance(it) }
        val hashFunction = MurmurHash3()
        val consistentHashRouter = ConsistentHashRouter(nodes, VIRTUAL_NODE_COUNT, hashFunction)
        val instance = consistentHashRouter.routeNode(keyValueSaveRequest.key) ?: throw RuntimeException()

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
        val nodes = keyValueRoutesProperties.nodes.map { Instance(it) }
        val hashFunction = MurmurHash3()
        val consistentHashRouter = ConsistentHashRouter(nodes, VIRTUAL_NODE_COUNT, hashFunction)
        val instance = consistentHashRouter.routeNode(key) ?: throw RuntimeException()

        val nodeIp = instance.getKey()

        return try {
            restTemplate.getForObject("$nodeIp?key=$key", KeyValueGetResponse::class.java)!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }

    companion object {
        private const val VIRTUAL_NODE_COUNT = 10
    }
}
