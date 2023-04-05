package com.tommy.proxy.consistenthashing.hash

import com.google.common.hash.Hashing
import kotlin.random.Random
import org.springframework.stereotype.Component

@Component
class MurmurHash3 : HashFunction {

    override fun doHash(key: String, seed: Int?): Int {
        return Hashing
            .murmur3_32_fixed(seed ?: generateMurmurHash3Seed())
            .hashString(key, Charsets.UTF_8)
            .asInt()
    }

    fun generateMurmurHash3Seed(): Int {
        return Random.nextLong().toInt()
    }
}
