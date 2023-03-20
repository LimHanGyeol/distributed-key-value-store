package com.tommy.proxy.consistenthashing.node

class VirtualNode<T : Node>(
    val physicalNode: T,
    private val virtualIndex: Int,
) : Node {

    override fun getKey(): String {
        return "${physicalNode.getKey()}-$virtualIndex"
    }

    fun isVirtualNodeOf(physicalNode: T): Boolean {
        return this.physicalNode.getKey() == physicalNode.getKey()
    }
}
