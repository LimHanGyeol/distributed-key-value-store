package com.tommy.proxy.dtos

data class NodeRegisterRequest(
    val address: String,
    val virtualNodeCount: Int,
)
