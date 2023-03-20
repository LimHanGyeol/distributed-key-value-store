package com.tommy.keyvaluestore.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tommy.keyvaluestore.dtos.KeyValueGetResponse
import com.tommy.keyvaluestore.dtos.KeyValueSaveRequest
import com.tommy.keyvaluestore.dtos.KeyValueSaveResponse
import com.tommy.keyvaluestore.services.KeyValueService
import io.mockk.every
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

@WebMvcTest(KeyValueController::class)
class KeyValueControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean private val keyValueService: KeyValueService,
) {

    @Test
    @DisplayName("key value 가 입력되면 이를 메모리에 저장한다.")
    fun `sut should return KeyValueSaveResponse when keyValueRequest is given`() {
        // Arrange
        val keyValueSaveRequest = KeyValueSaveRequest(key = "name", value = "hangyeol")

        every { keyValueService.put(keyValueSaveRequest) } returns KeyValueSaveResponse(keyValueSaveRequest.key)

        // Act
        val actual = mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyValueSaveRequest)),
        ).andDo(print())

        // Assert
        verify { keyValueService.put(any()) }

        actual.andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("name"))
    }

    @Test
    @DisplayName("key 에 해당하는 값이 존재할 경우 KeyValueGetResponse 를 응답한다.")
    fun `sut should return KeyValueGetResponse when exist key is given`() {
        // Arrange
        val value = "hangyeol"

        every { keyValueService.get("name") } returns KeyValueGetResponse(value)

        // Act
        val actual = mockMvc.perform(
            get("/")
                .param("key", "name")
                .accept(MediaType.APPLICATION_JSON),
        ).andDo(print())

        // Assert
        verify { keyValueService.get(any()) }

        actual.andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value("hangyeol"))
    }

    @Test
    @DisplayName("key 에 해당하는 값이 존재하지 않을 경우 KeyValueGetResponse(null) 을 응답한다.")
    fun `sut should return Null KeyValueResponse when not exist key is given`() {
        // Arrange
        every { keyValueService.get("name") } returns KeyValueGetResponse(null)

        // Act
        val actual = mockMvc.perform(
            get("/")
                .param("key", "name")
                .accept(MediaType.APPLICATION_JSON),
        ).andDo(print())

        // Assert
        verify { keyValueService.get(any()) }

        actual.andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value(null))
    }
}
