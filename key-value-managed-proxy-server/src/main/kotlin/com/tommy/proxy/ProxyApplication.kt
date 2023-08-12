package com.tommy.proxy

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(scanBasePackages = ["com.tommy.proxy"])
class ProxyApplication

fun main(args: Array<String>) {
    runApplication<ProxyApplication>(*args)
}

@RestController
class IndexController(
    @Value("\${spring.application.name:unknown}") private val appName: String
) {

    @RequestMapping(
        value = ["/"],
        method = [RequestMethod.GET, RequestMethod.HEAD],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    fun healthcheck() = "${appName.uppercase()}: healthy"
}
