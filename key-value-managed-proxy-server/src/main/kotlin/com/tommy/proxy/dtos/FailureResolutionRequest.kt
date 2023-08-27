package com.tommy.proxy.dtos

data class FailureResolutionRequest(
    val address: String,
    val heartBeatCount: Int,
)
