package com.tommy.keyvaluestore.dto

data class FailureResolutionRequest(
    val address: String,
    val heatBeatCount: Int,
)
