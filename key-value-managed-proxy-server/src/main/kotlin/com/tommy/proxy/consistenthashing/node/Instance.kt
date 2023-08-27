package com.tommy.proxy.consistenthashing.node

class Instance(
    private val ip: String,
) : Node {

    override fun getKey(): String = "http://$ip"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Instance

        if (ip != other.ip) return false

        return true
    }

    override fun hashCode(): Int {
        return ip.hashCode()
    }

    override fun toString(): String = getKey()
}
