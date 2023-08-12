package com.tommy.keyvaluestore.dtos

data class NodeRegisterRequest(
    val address: String,
    val virtualNodeCount: Int,
)
