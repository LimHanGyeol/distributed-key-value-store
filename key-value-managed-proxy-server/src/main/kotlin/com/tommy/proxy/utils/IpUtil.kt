package com.tommy.proxy.utils

import jakarta.servlet.http.HttpServletRequest

object IpUtil {

    private val IP_HEADER_CANDIDATES = arrayOf(
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR",
    )

    fun getClientIp(request: HttpServletRequest): String {
        var ip = getIpByXForwardedFor(request)

        for (ipHeader in IP_HEADER_CANDIDATES) {
            ip = request.getHeader(ipHeader)

            if (!invalidIp(ip)) {
                return ip
            }
        }
        return ip
    }

    private fun getIpByXForwardedFor(request: HttpServletRequest): String {
        var ip = request.getHeader("X-Forwarded-For")

        if (ip.contains(",")) {
            ip = ip.split(",").first()
        }
        return ip
    }

    private fun invalidIp(ip: String?): Boolean {
        return ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)
    }
}
