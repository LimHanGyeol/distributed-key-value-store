package com.tommy.proxy.consistenthashing

import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.consistenthashing.node.Node
import com.tommy.proxy.consistenthashing.node.VirtualNode
import org.springframework.stereotype.Component
import java.util.SortedMap
import java.util.TreeMap

@Component
class ConsistentHashRouter(
    private val hashFunction: HashFunction,
) {
    private val originHashRing: TreeMap<Long, VirtualNode<Node>> = TreeMap()
    private lateinit var replicaHashRing: TreeMap<Long, VirtualNode<Node>>

    fun addNode(physicalNode: Node, virtualNodeCount: Int) {
        if (virtualNodeCount < 0) {
            throw IllegalArgumentException("invalid virtual node counts: $virtualNodeCount")
        }

        if (originHashRing.isEmpty()) {
            addFirstNode(physicalNode, virtualNodeCount)
            return
        }
        addOtherNode(physicalNode, virtualNodeCount)
    }

    private fun addFirstNode(physicalNode: Node, virtualNodeCount: Int) {
        val virtualNodeIndex = virtualNodeCount - 1
        originHashRing[Long.MIN_VALUE] = VirtualNode(physicalNode, FIRST_NODE_INDEX)
        originHashRing[Long.MAX_VALUE] = VirtualNode(physicalNode, virtualNodeIndex)
        addVirtualNodes(physicalNode, 1, virtualNodeIndex)
    }

    private fun addOtherNode(physicalNode: Node, virtualNodeCount: Int) {
        addVirtualNodes(physicalNode, FIRST_NODE_INDEX, virtualNodeCount)
    }

    private fun addVirtualNodes(physicalNode: Node, start: Int, end: Int) {
        (start until end).forEach { i ->
            val virtualNode = VirtualNode(physicalNode, i)
            val hashedKey = hashFunction.doHash(virtualNode.getKey())
            originHashRing[hashedKey] = virtualNode
        }
    }

    fun removeNode(physicalNode: Node) {
        originHashRing.entries.removeIf { it.value.isVirtualNodeOf(physicalNode) }
    }

    fun routeNode(hashedKey: Long): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val tailMap: SortedMap<Long, VirtualNode<Node>> = originHashRing.tailMap(hashedKey)

        val hashedNodeValue = if (tailMap.isNotEmpty()) {
            tailMap.firstKey()
        } else {
            originHashRing.firstKey()
        }

        val virtualNode = originHashRing[hashedNodeValue] ?: originHashRing[originHashRing.lastKey()]

        return virtualNode?.physicalNode
            ?: throw IllegalStateException("not found exist node. hashed node value is $hashedNodeValue")
    }

    fun routeOtherNode(hashedKey: Long, primaryNode: Node): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val tailMap: SortedMap<Long, VirtualNode<Node>> = originHashRing.tailMap(hashedKey)

        if (tailMap.isEmpty() || tailMap.size <= 3) { // TODO: 총 노드의 10%
            val firstVirtualNode = originHashRing[originHashRing.firstKey()]!!
            return if (firstVirtualNode.isVirtualNodeOf(primaryNode)) {
                originHashRing.values.first { it.physicalNode != firstVirtualNode.physicalNode }.physicalNode
            } else {
                firstVirtualNode.physicalNode
            }
        }

        val iterator = tailMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()

            val virtualNode = originHashRing[key] ?: originHashRing[originHashRing.lastKey()]!!

            if (virtualNode.isVirtualNodeOf(primaryNode)) {
                continue
            }
            return virtualNode.physicalNode
        }
        throw IllegalStateException("not found exist node. hashedKey is $hashedKey")
    }

    fun replicateHashRing() {
        replicaHashRing = TreeMap(originHashRing)
    }

    fun getExistingVirtualNodeCount(physicalNode: Node): Int = originHashRing.values.count {
        it.isVirtualNodeOf(
            physicalNode,
        )
    }

    fun doHash(key: String): Long = hashFunction.doHash(key)

    fun getOriginHashRingSize(): Int = originHashRing.size

    fun getReplicaHashRingSize(): Int = replicaHashRing.size

    companion object {
        private const val FIRST_NODE_INDEX = 0
    }
}
