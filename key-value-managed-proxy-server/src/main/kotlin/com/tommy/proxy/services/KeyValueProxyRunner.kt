package com.tommy.proxy.services

import com.tommy.proxy.consistenthashing.ConsistentHashRouter
import com.tommy.proxy.utils.IpUtil
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class KeyValueProxyRunner(
    val consistentHashRouter: ConsistentHashRouter,
) : ApplicationRunner {

    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        val hostIp = IpUtil.getCurrentIp()
        val seed = IpUtil.getIpToInteger(hostIp)
        consistentHashRouter.initNodes(seed)
        logger.info { "consistentHashRouter init nodes, hashring Size: ${consistentHashRouter.getHashRingSize()}" }
    }
}
