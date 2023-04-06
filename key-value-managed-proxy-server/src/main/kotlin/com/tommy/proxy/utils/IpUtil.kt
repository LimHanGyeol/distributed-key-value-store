package com.tommy.proxy.utils

import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

object IpUtil {

    fun getCurrentIp(): String = InetAddress.getLocalHost().hostAddress

    /**
     * 현재 Instance 의 IP 를 바이트 배열로 변환한 후 이를 32비트 정수로 변환한다.
     * 이를 MurmurHash의 seed 값으로 사용한다. 그리하여 동적이되 간극이 크지 않은 seed를 사용할 수 있다.
     */
    fun getIpToInteger(ipAddress: String): Int {
        val inetAddress = InetAddress.getByName(ipAddress)
        val ipAddressBytes = inetAddress.address

        val byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
        byteBuffer.put(ipAddressBytes)
        byteBuffer.flip()
        return byteBuffer.int
    }
}
