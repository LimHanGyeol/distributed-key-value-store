package com.tommy.keyvaluestore.services

import com.tommy.keyvaluestore.dtos.NodeRegisterRequest
import com.tommy.keyvaluestore.dtos.NodeRegisterResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.InetAddress

@Component
class KeyValueApplicationRunner(
    @Value("\${server.port}") private val port: String,
    private val restTemplate: RestTemplate,
) : ApplicationRunner {

    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        val address = InetAddress.getLocalHost().hostAddress
        val request = NodeRegisterRequest("$address:$port", 10)

        try {
            val response =
                restTemplate.postForObject("http://localhost:8080/node", request, NodeRegisterResponse::class.java)

            logger.info { "Address Node Count: ${response?.existsNodeCount}" }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
