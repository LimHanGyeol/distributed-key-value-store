package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class KeyValueProxyRunner(
    @Value("\${server.port}") val port: Int,
    val consistentHashRouter: ConsistentHashRouter,
) : ApplicationRunner {

    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        consistentHashRouter.initNodes(port)
        logger.info { "consistentHashRouter init nodes, hashring Size: ${consistentHashRouter.getHashRingSize()}" }
    }
}
