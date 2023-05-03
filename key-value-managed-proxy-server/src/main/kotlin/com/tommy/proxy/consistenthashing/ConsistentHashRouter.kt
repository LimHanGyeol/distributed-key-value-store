package com.tommy.proxy.consistenthashing

import com.tommy.proxy.config.KeyValueRoutesProperties
import com.tommy.proxy.consistenthashing.hash.HashFunction
import com.tommy.proxy.consistenthashing.node.Instance
import com.tommy.proxy.consistenthashing.node.Node
import com.tommy.proxy.consistenthashing.node.VirtualNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.TreeMap
import kotlin.math.absoluteValue

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
        val previousVirtualNodes = originHashRing.values

        val newVirtualNodes = (1..virtualNodeCount).map {
            VirtualNode(physicalNode, it)
        }
        val virtualNodes = previousVirtualNodes + newVirtualNodes
        val spreadNodes = virtualNodes.sortedBy { hashFunction.doHash(it.getKey()) }

        originHashRing.clear()

        spreadNodes.forEachIndexed { i, virtualNode -> originHashRing[i] = virtualNode }
    }

    fun removeNode(physicalNode: Node) {
        val removedVirtualNodes = originHashRing.filterNot { it.value.isVirtualNodeOf(physicalNode) }.values
        val remainingHashRing = removedVirtualNodes.withIndex().associate { (i, it) -> i to it }

        originHashRing.clear()
        originHashRing.putAll(remainingHashRing)
    }

    fun routeNode(key: String): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val hashedKey = hashFunction.doHash(key)
        val hashRingIndex = hashedKey % originHashRing.size

        val virtualNode = originHashRing[hashRingIndex.absoluteValue]
            ?: throw IllegalStateException("not found exist node. hashed node value is $key")

        return virtualNode.physicalNode
    }

    fun routeOtherNode(key: String, primaryNode: Node): Node {
        if (originHashRing.isEmpty()) {
            throw IllegalStateException("hashRing is Empty !")
        }

        val hashedKey = hashFunction.doHash(key)
        val hashRingIndex = hashedKey % originHashRing.size

        val iterator = originHashRing.keys.iterator()
        while (iterator.hasNext()) {
            val virtualNode = originHashRing[hashRingIndex.absoluteValue]
                ?: throw IllegalStateException("not found exist node. hashed node value is $key")

            if (virtualNode.isVirtualNodeOf(primaryNode)) {
                val otherVirtualNodes = originHashRing.filterNot { it.value.isVirtualNodeOf(primaryNode) }.values

                val otherNodeHashRing = otherVirtualNodes.withIndex().associate { (i, it) -> i to it }

                val otherHashRingIndex = hashedKey % otherNodeHashRing.size
                val secondaryVirtualNode = otherNodeHashRing[otherHashRingIndex.absoluteValue]
                    ?: throw IllegalStateException("not found exist node. hashed node value is $key")

                return secondaryVirtualNode.physicalNode
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

    fun getOriginHashRingSize(): Int = originHashRing.size

    fun getReplicaHashRingSize(): Int = replicaHashRing.size
}
