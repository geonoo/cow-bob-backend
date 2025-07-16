package com.logistics.exception

import com.logistics.controller.DeliveryController
import com.logistics.service.DeliveryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any

@WebMvcTest(DeliveryController::class)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var deliveryService: DeliveryService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `ResourceNotFoundException 처리 테스트`() {
        // Given
        whenever(deliveryService.getDeliveryById(999L))
            .thenThrow(ResourceNotFoundException("ID가 999 인 배송을 찾을 수 없습니다."))

        // When & Then
        mockMvc.perform(get("/api/deliveries/999"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("ID가 999 인 배송을 찾을 수 없습니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    @Test
    fun `BusinessRuleViolationException 처리 테스트`() {
        // Given
        whenever(deliveryService.deleteDelivery(1L))
            .thenThrow(BusinessRuleViolationException("진행 중인 배송은 삭제할 수 없습니다."))

        // When & Then
        mockMvc.perform(delete("/api/deliveries/1"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Business Rule Violation"))
            .andExpect(jsonPath("$.message").value("진행 중인 배송은 삭제할 수 없습니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    @Test
    fun `InvalidRequestException 처리 테스트`() {
        // Given
        whenever(deliveryService.createDelivery(any()))
            .thenThrow(InvalidRequestException("배송지는 필수 입력 항목입니다."))

        val invalidDeliveryJson = """
            {
                "destination": "",
                "address": "서울시 강남구",
                "price": 100000,
                "feedTonnage": 5.0,
                "deliveryDate": "2024-12-31"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidDeliveryJson))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Request"))
            .andExpect(jsonPath("$.message").value("배송지는 필수 입력 항목입니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    @Test
    fun `AssignmentException 처리 테스트`() {
        // Given
        whenever(deliveryService.assignDeliveryToDriver(1L, 1L))
            .thenThrow(AssignmentException("배차 처리 중 오류가 발생했습니다."))

        // When & Then
        mockMvc.perform(post("/api/deliveries/1/assign/1"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Assignment Error"))
            .andExpect(jsonPath("$.message").value("배차 처리 중 오류가 발생했습니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    @Test
    fun `DataIntegrityException 처리 테스트`() {
        // Given
        whenever(deliveryService.createDelivery(any()))
            .thenThrow(DataIntegrityException("데이터 무결성 오류가 발생했습니다."))

        val deliveryJson = """
            {
                "destination": "서울농장",
                "address": "서울시 강남구",
                "price": 100000,
                "feedTonnage": 5.0,
                "deliveryDate": "2024-12-31"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(deliveryJson))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Data Integrity Error"))
            .andExpect(jsonPath("$.message").value("데이터 무결성 오류가 발생했습니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    @Test
    fun `일반 Exception 처리 테스트`() {
        // Given
        whenever(deliveryService.getAllDeliveries())
            .thenThrow(RuntimeException("예상치 못한 오류"))

        // When & Then
        mockMvc.perform(get("/api/deliveries"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").exists())
    }
}