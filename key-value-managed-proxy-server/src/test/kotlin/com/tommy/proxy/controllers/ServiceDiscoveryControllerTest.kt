package com.tommy.proxy.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tommy.proxy.dtos.NodeRegisterRequest
import com.tommy.proxy.dtos.NodeRegisterResponse
import com.tommy.proxy.services.ServiceDiscoveryService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ServiceDiscoveryController::class)
class ServiceDiscoveryControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean private val serviceDiscoveryService: ServiceDiscoveryService,
) {

    @Test
    @DisplayName("서비스의 Node 등록 요청이 오면 이를 안정 해시의 해시링에 등록한다.")
    fun `sut register service when node register request is given`() {
        // Arrange
        val nodeRegisterRequest = NodeRegisterRequest(address = "127.0.0.1:8080", virtualNodeCount = 10)
        val nodeRegisterResponse = NodeRegisterResponse(
            existsNodeCount = nodeRegisterRequest.virtualNodeCount,
        )

        every { serviceDiscoveryService.addNode(nodeRegisterRequest) } returns nodeRegisterResponse

        // Act & Assert
        mockMvc.perform(
            post("/node")
                .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nodeRegisterRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.existsNodeCount").value(nodeRegisterResponse.existsNodeCount))
            .andDo(print())

        verify { serviceDiscoveryService.addNode(nodeRegisterRequest) }
    }
}
