package com.tommy.proxy.consistenthashing

import com.tommy.proxy.config.KeyValueRoutesProperties
import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.consistenthashing.node.Node
import com.tommy.proxy.consistenthashing.node.VirtualNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.SortedMap
import java.util.TreeMap

@Component
class ConsistentHashRouter(
    @Value("\${key-value.virtual-node-count:0}") private val virtualNodeCount: Int,
    private val keyValueRoutesProperties: KeyValueRoutesProperties,
    private val hashFunction: HashFunction,
) {
    private val originHashRing: TreeMap<Int, VirtualNode<Node>> = TreeMap()
    private lateinit var replicaHashRing: TreeMap<Int, VirtualNode<Node>>

    fun initNodes() {
        val physicalNodes: List<Node> = keyValueRoutesProperties.nodes.map { Instance(it) }
        for (physicalNode in physicalNodes) {
            addNode(physicalNode, virtualNodeCount)
        }
    }

    fun addNode(physicalNode: Node, virtualNodeCount: Int) {
        if (virtualNodeCount < 0) {
            throw IllegalArgumentException("invalid virtual node counts: $virtualNodeCount")
        }
        val existingReplicas = getExistingVirtualNodeCount(physicalNode)
        for (i in 0 until virtualNodeCount) {
            val virtualNode = VirtualNode(physicalNode, i + existingReplicas)
            val hashedKey = hashFunction.doHash(virtualNode.getKey())
            originHashRing[hashedKey] = virtualNode
        }
    }

    fun removeNode(physicalNode: Node) {
        val iterator: MutableIterator<Int> = originHashRing.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val virtualNode = originHashRing[key]

            if (virtualNode?.isVirtualNodeOf(physicalNode) == true) {
                iterator.remove()
            }
        }
    }

    fun routeNode(hashedKey: Int): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val tailMap: SortedMap<Int, VirtualNode<Node>> = originHashRing.tailMap(hashedKey)

        val hashedNodeValue = if (tailMap.isNotEmpty()) {
            tailMap.firstKey()
        } else {
            originHashRing.firstKey()
        }

        val virtualNode = originHashRing[hashedNodeValue] ?: originHashRing[originHashRing.lastKey()]

        return virtualNode?.physicalNode
            ?: throw IllegalStateException("not found exist node. hashed node value is $hashedNodeValue")
    }

    fun routeOtherNode(hashedKey: Int, primaryNode: Node): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val tailMap: SortedMap<Int, VirtualNode<Node>> = originHashRing.tailMap(hashedKey)

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

    fun doHash(key: String): Int = hashFunction.doHash(key)

    fun getOriginHashRingSize(): Int = originHashRing.size

    fun getReplicaHashRingSize(): Int = replicaHashRing.size
}
