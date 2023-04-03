package com.tommy.proxy.consistenthashing

import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.consistenthashing.hash.MD5Hash
import com.tommy.proxy.consistenthashing.node.Node
import com.tommy.proxy.consistenthashing.node.VirtualNode
import java.util.SortedMap
import java.util.TreeMap

class ConsistentHashRouter<T : Node>(
    physicalNodes: List<T>,
    virtualNodeCount: Int,
    private val hashFunction: HashFunction = MD5Hash(),
) {
    private val hashRing: TreeMap<Int, VirtualNode<T>> = TreeMap()

    init {
        for (physicalNode in physicalNodes) {
            addNode(physicalNode, virtualNodeCount)
        }
    }

    fun addNode(physicalNode: T, virtualNodeCount: Int) {
        if (virtualNodeCount < 0) {
            throw IllegalArgumentException("invalid virtual node counts: $virtualNodeCount")
        }
        val existingReplicas = getExistingVirtualIndex(physicalNode)
        for (i in 0 until virtualNodeCount) {
            val virtualNode = VirtualNode(physicalNode, i + existingReplicas)
            val hashedKey = hashFunction.doHash(virtualNode.getKey())
            hashRing[hashedKey] = virtualNode
        }
    }

    fun removeNode(physicalNode: T) {
        val iterator: MutableIterator<Int> = hashRing.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val virtualNode = hashRing[key]

            if (virtualNode?.isVirtualNodeOf(physicalNode) == true) {
                iterator.remove()
            }
        }
    }

    fun routeNode(key: String): T? {
        if (hashRing.isEmpty()) {
            return null
        }

        val hashValue = hashFunction.doHash(key)
        val tailMap: SortedMap<Int, VirtualNode<T>> = hashRing.tailMap(hashValue)

        val nodeHashValue = if (tailMap.isNotEmpty()) {
            tailMap.firstKey()
        } else {
            hashRing.firstKey()
        }

        val virtualNode = hashRing[nodeHashValue]
        return virtualNode?.physicalNode
    }

    private fun getExistingVirtualIndex(physicalNode: T): Int =
        hashRing.values.count { it.isVirtualNodeOf(physicalNode) }

    fun getHashRingSize(): Int = hashRing.size
}
