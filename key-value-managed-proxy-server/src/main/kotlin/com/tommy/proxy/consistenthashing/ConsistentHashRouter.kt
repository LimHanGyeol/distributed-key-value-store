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

    fun initNodes(seed: Int? = null) {
        val physicalNodes: List<Node> = keyValueRoutesProperties.nodes.map { Instance(it) }
        for (physicalNode in physicalNodes) {
            addNode(physicalNode, virtualNodeCount, seed)
        }
    }

    fun addNode(physicalNode: Node, virtualNodeCount: Int, seed: Int? = null) {
        if (virtualNodeCount < 0) {
            throw IllegalArgumentException("invalid virtual node counts: $virtualNodeCount")
        }
        val existingReplicas = getExistingVirtualNodeCount(physicalNode)
        for (i in 0 until virtualNodeCount) {
            val virtualNode = VirtualNode(physicalNode, i + existingReplicas)
            val hashedKey = hashFunction.doHash(virtualNode.getKey(), seed)
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

        val virtualNode = originHashRing[hashedNodeValue]
            ?: throw IllegalStateException("not found exist node. hashed node value is $hashedNodeValue")

        return virtualNode.physicalNode
    }

    fun routeOtherNode(hashedKey: Int, primaryNode: Node): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val tailMap: SortedMap<Int, VirtualNode<Node>> = originHashRing.tailMap(hashedKey)

        val iterator = tailMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()

            val virtualNode =
                originHashRing[key] ?: throw IllegalStateException("not found exist node. hashed node value is $key")

            if (virtualNode.isVirtualNodeOf(primaryNode)) {
                continue
            }
            return virtualNode.physicalNode
        }
        throw IllegalStateException("no matching virtual node found !")
    }

    fun replicateHashRing() {
        replicaHashRing = TreeMap(originHashRing)
    }

    fun getExistingVirtualNodeCount(physicalNode: Node): Int = originHashRing.values.count {
        it.isVirtualNodeOf(
            physicalNode,
        )
    }

    fun doHash(key: String, seed: Int?): Int = hashFunction.doHash(key, seed)

    fun getOriginHashRingSize(): Int = originHashRing.size

    fun getReplicaHashRingSize(): Int = replicaHashRing.size
}
