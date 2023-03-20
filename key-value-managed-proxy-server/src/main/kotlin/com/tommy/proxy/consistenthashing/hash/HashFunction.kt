package com.tommy.proxy.consistenthashing.hash

interface HashFunction {

    fun doHash(key: String): Long
}
