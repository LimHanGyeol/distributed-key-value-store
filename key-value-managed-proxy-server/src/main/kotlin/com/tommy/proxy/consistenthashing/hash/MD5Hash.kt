package com.tommy.proxy.consistenthashing.hash

import java.security.MessageDigest

class MD5Hash : HashFunction {

    private val instance: MessageDigest = MessageDigest.getInstance(HASH_ALGORITHM)

    override fun doHash(key: String, seed: Int?): Int {
        instance.reset()

        val keyToByte = key.byteInputStream()

        instance.update(keyToByte.readBytes())

        val digest = instance.digest()

        var hash = 0
        (0..3).forEach { i ->
            hash = hash shl 8
            hash = hash or ((digest[i].toInt() and 0xFF))
        }
        return hash
    }

    companion object {
        private const val HASH_ALGORITHM = "MD5"
    }
}
