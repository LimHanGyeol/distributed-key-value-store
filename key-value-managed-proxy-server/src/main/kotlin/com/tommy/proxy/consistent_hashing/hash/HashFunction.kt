package com.tommy.proxy.consistent_hashing.hash

interface HashFunction {

    fun doHash(key: String): Long
}
