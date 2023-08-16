package com.tommy.proxy.consistenthashing.hash

import com.google.common.hash.Hashing
import org.springframework.stereotype.Component

@Component
class MurmurHash3 : HashFunction {

    override fun doHash(key: String): Long {
        return Hashing.murmur3_128()
            .hashString(key, Charsets.UTF_8)
            .asLong()
    }
}
