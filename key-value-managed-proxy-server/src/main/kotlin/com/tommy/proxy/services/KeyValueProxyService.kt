package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class KeyValueProxyService(
    private val webClient: WebClient,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val masterNodeIp = node1.getKey()

        val otherNodes = listOf(node1, node2, node3, node4)
            .filterNot { it.getKey() == masterNodeIp }
            .toList()

        val masterNodeResponse = callKeyValueSave(masterNodeIp, keyValueSaveRequest)
            .block()

        if (masterNodeResponse?.statusCode?.is2xxSuccessful == true) {
            otherNodes.forEach {
                val mono = callKeyValueSave(it.getKey(), keyValueSaveRequest)
                mono.subscribeOn(Schedulers.boundedElastic()).subscribe()
            }
        }
        return masterNodeResponse?.body!!
    }

    fun callKeyValueSave(
        nodeIp: String,
        keyValueSaveRequest: KeyValueSaveRequest,
    ): Mono<ResponseEntity<KeyValueSaveResponse>> {
        return webClient.mutate()
            .baseUrl(nodeIp)
            .build()
            .post()
            .bodyValue(keyValueSaveRequest)
            .retrieve()
            .toEntity(KeyValueSaveResponse::class.java)
    }

    fun get(key: String): KeyValueGetResponse {
        val consistentHashRouter = ConsistentHashRouter(nodes, VIRTUAL_NODE_COUNT)
        val instance = consistentHashRouter.routeNode(key) ?: throw RuntimeException()

        val nodeIp = instance.getKey()

        return try {
            val keyValueGetResponse = webClient.mutate()
                .baseUrl(nodeIp)
                .build()
                .get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                        .queryParam("key", key)
                        .build()
                }
                .retrieve()
                .bodyToMono<KeyValueGetResponse>()
                .block()

            keyValueGetResponse!!
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
