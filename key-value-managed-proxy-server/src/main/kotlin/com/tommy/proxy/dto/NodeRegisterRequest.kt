package com.tommy.proxy.dto

data class NodeRegisterRequest(
    val address: String,
    val virtualNodeCount: Int,
)
