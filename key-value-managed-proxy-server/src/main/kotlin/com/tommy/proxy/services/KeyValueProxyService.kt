package com.tommy.proxy.services

import com.tommy.proxy.config.KeyValueRoutesProperties
import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class KeyValueProxyService(
    private val webClient: WebClient,
    private val keyValueRoutesProperties: KeyValueRoutesProperties,
) {
    private val logger = KotlinLogging.logger { }

    fun put(keyValueSaveRequest: KeyValueSaveRequest): KeyValueSaveResponse {
        val masterNodeIp = keyValueRoutesProperties.nodes.first()

        val otherNodes = keyValueRoutesProperties.nodes
            .filterNot { it == masterNodeIp }
            .toList()

        // TODO: Master 에서 장애 발생 시 이를 감지하여 다른 노드로 전송하도록 해야한다.
        val masterNodeResponse = callKeyValueSave(masterNodeIp, keyValueSaveRequest).block()

        if (masterNodeResponse?.statusCode?.is2xxSuccessful == true) {
            otherNodes.forEach {
                val mono = callKeyValueSave(it, keyValueSaveRequest)
                mono.subscribeOn(Schedulers.boundedElastic()).subscribe { println("Call Node : $it") }
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
            .onStatus(
                { httpStatusCode: HttpStatusCode? -> httpStatusCode?.is5xxServerError == true },
                { response: ClientResponse -> Mono.error(Throwable("Internal Server Error - try again later")) },
            )
            .toEntity(KeyValueSaveResponse::class.java)
    }

    fun get(key: String): KeyValueGetResponse {
        val nodes = keyValueRoutesProperties.nodes.map { Instance(it) }
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
    }
}
