package com.tommy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KeyValueStoreApplication

fun main(args: Array<String>) {
    runApplication<KeyValueStoreApplication>(*args)
}
