package com.tommy.proxy.controllers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    @RequestMapping(
        value = ["/"],
        method = [RequestMethod.GET, RequestMethod.HEAD],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    fun healthcheck() = "Who ar you?"
}
