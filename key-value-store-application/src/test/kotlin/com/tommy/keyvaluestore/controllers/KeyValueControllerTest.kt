package com.tommy.keyvaluestore.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tommy.keyvaluestore.dtos.KeyValueRequest
import com.tommy.keyvaluestore.dtos.KeyValueResponse
import com.tommy.keyvaluestore.services.KeyValueService
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

@WebMvcTest(KeyValueController::class)
class KeyValueControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean private val keyValueService: KeyValueService,
) {

    @Test
    @DisplayName("key value 가 입력되면 이를 메모리에 저장한다.")
    fun `sut should return KeyValueResponse when keyValueRequest is given`() {
        // Arrange
        val keyValueRequest = KeyValueRequest(key = "name", value = "hangyeol")

        every { keyValueService.put(keyValueRequest) } returns KeyValueResponse(keyValueRequest.key)

        // Act
        val actual = mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyValueRequest)),
        ).andDo(print())

        // Assert
        verify { keyValueService.put(any()) }

        actual.andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("name"))
    }
}
