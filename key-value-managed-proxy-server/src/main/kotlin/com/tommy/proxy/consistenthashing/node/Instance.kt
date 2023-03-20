package com.tommy.proxy.consistenthashing.node

class Instance(
    private val ip: String,
    private val port: Int
) : Node {

    override fun getKey(): String = "$ip:$port"

    override fun toString(): String = getKey()
}
