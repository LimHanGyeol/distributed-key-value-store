package com.tommy.proxy.dtos

class FaultNodeRequest(
    val address: String,
    val heatBeatCount: Int,
) {
    override fun toString(): String {
        return "FaultNodeRequest(address='$address', heatBeatCount=$heatBeatCount)"
    }
}
