package com.tommy.proxy.dto

data class FailureResolutionRequest(
    val address: String,
    val heartBeatCount: Int,
)
