package com.tommy.proxy.consistenthashing.node

class Instance(
    private val ip: String,
) : Node {

    override fun getKey(): String = ip

    override fun toString(): String = getKey()
}
