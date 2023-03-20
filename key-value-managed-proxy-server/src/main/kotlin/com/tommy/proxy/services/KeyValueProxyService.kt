package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
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
) {
    private val logger = KotlinLogging.logger { }

    fun put(requestIP: String, keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val consistentHashRouter = ConsistentHashRouter(nodes, VIRTUAL_NODE_COUNT)
        val instance = consistentHashRouter.routeNode(requestIP) ?: throw RuntimeException()

        val nodeIp = instance.getKey()

        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val responseEntity = restTemplate.postForEntity(
                nodeIp,
                HttpEntity(keyValueSaveRequest, headers),
                KeyValueSaveResponse::class.java,
            )

            responseEntity.body!!
        } catch (e: Exception) {
            logger.error { e.message }
            throw RuntimeException(e.message)
        }
    }

    companion object {
        private const val VIRTUAL_NODE_COUNT = 10

        // TODO: 외부 인스턴스를 주입하도록 변경
        private val node1 = Instance("127.0.0.1", 80)
        private val node2 = Instance("127.0.0.2", 80)
        private val node3 = Instance("127.0.0.3", 80)
        private val node4 = Instance("127.0.0.4", 80)

        private val nodes = listOf(node1, node2, node3, node4)
    }
}
