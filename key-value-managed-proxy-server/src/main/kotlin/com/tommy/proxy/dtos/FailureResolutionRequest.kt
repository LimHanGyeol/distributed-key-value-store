package com.tommy.proxy.dtos

class FailureResolutionRequest(
    val address: String,
    val heartBeatCount: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FailureResolutionRequest

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

    override fun toString(): String {
        return "FaultNodeRequest(address='$address', heatBeatCount=$heartBeatCount)"
    }
}
