package com.tommy.proxy.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tommy.proxy.dtos.FailureResolutionRequest
import com.tommy.proxy.dtos.KeyValueGetResponse
import com.tommy.proxy.dtos.KeyValueSaveRequest
import com.tommy.proxy.dtos.KeyValueSaveResponse
import com.tommy.proxy.services.FailureResolutionService
import com.tommy.proxy.services.KeyValueProxyService
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(KeyValueProxyController::class)
class KeyValueProxyControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean private val keyValueProxyService: KeyValueProxyService,
    @MockkBean private val failureResolutionService: FailureResolutionService,
) {

    @Test
    @DisplayName("key-value 저장 요청이 오면 이를 분산 처리 후 keyValueSaveResponse 를 응답한다.")
    fun `sut should return KeyValueSaveResponse when keyValueSaveRequest is given`() {
        // Arrange
        val keyValueSaveRequest = KeyValueSaveRequest(key = "name", value = "hangyeol")

        every { keyValueProxyService.put(keyValueSaveRequest) } returns KeyValueSaveResponse(keyValueSaveRequest.key)

        // Act & Assert
        mockMvc.perform(
            post("/put")
                .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyValueSaveRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("name"))
            .andDo(print())

        verify { keyValueProxyService.put(keyValueSaveRequest) }
    }

    @Test
    @DisplayName("key 조회 요청이 오면 이를 조회하여 KeyValueGetResponse 를 응답한다.")
    fun `sut should return KeyValueGetResponse when key is given`() {
        // Arrange
        val key = "name"

        every { keyValueProxyService.get(key) } returns KeyValueGetResponse("hangyeol")

        // Act & Assert
        mockMvc.perform(
            get("/get")
                .param("key", key)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value("hangyeol"))
            .andDo(print())

        verify { keyValueProxyService.get(key) }
    }

    @Test
    @DisplayName("장애 발생 노드 요청이 오면 이에대한 장애 처리를 수행한다.")
    fun `sut should resolve failure node`() {
        // Arrange
        val failureResolutionRequest = FailureResolutionRequest("http://127.0.0.1:8081", 2)

        justRun { failureResolutionService.resolveFailureNode(failureResolutionRequest) }

        // Act & Assert
        mockMvc.perform(
            post("/failure-resolution")
                .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failureResolutionRequest)),
        )
            .andExpect(status().isOk)
            .andDo(print())

        verify { failureResolutionService.resolveFailureNode(failureResolutionRequest) }
    }
}
