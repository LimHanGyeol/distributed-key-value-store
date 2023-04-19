package com.tommy.keyvaluestore.dtos

data class FailureResolutionRequest(
    val address: String,
    val heatBeatCount: Int,
)
