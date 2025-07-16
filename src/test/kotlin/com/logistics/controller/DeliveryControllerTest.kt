package com.logistics.controller

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.service.DeliveryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
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
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(DeliveryController::class)
class DeliveryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var deliveryService: DeliveryService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testDelivery: Delivery
    private lateinit var testDriver: Driver

    @BeforeEach
    fun setUp() {
        testDriver = Driver(
            id = 1L,
            name = "김기사",
            phoneNumber = "010-1234-5678",
            vehicleNumber = "12가3456",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )

        testDelivery = Delivery(
            id = 1L,
            destination = "서울농장",
            address = "서울시 강남구 테헤란로 123",
            price = BigDecimal("500000"),
            feedTonnage = BigDecimal("3.5"),
            deliveryDate = LocalDate.now().plusDays(1),
            status = DeliveryStatus.PENDING
        )
    }

    @Test
    fun `모든 배송 조회 API 테스트`() {
        // Given
        val deliveries = listOf(testDelivery)
        whenever(deliveryService.getAllDeliveries()).thenReturn(deliveries)

        // When & Then
        mockMvc.perform(get("/api/deliveries"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].destination").value("서울농장"))
            .andExpect(jsonPath("$[0].feedTonnage").value(3.5))
            .andExpect(jsonPath("$[0].price").value(500000))
    }

    @Test
    fun `배송 생성 API 테스트`() {
        // Given
        whenever(deliveryService.createDelivery(any<Delivery>())).thenReturn(testDelivery)

        val deliveryJson = objectMapper.writeValueAsString(testDelivery)

        // When & Then
        mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(deliveryJson))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.destination").value("서울농장"))
            .andExpect(jsonPath("$.feedTonnage").value(3.5))
    }

    @Test
    fun `대기 중인 배송 조회 API 테스트`() {
        // Given
        val pendingDeliveries = listOf(testDelivery)
        whenever(deliveryService.getPendingDeliveries()).thenReturn(pendingDeliveries)

        // When & Then
        mockMvc.perform(get("/api/deliveries/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].status").value("PENDING"))
    }

    @Test
    fun `배차 할당 API 테스트`() {
        // Given
        val assignedDelivery = testDelivery.copy(
            driver = testDriver,
            status = DeliveryStatus.ASSIGNED
        )
        whenever(deliveryService.assignDeliveryToDriver(1L, 1L)).thenReturn(assignedDelivery)

        // When & Then
        mockMvc.perform(post("/api/deliveries/1/assign/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ASSIGNED"))
            .andExpect(jsonPath("$.driver.name").value("김기사"))
    }

    @Test
    fun `배송 완료 API 테스트`() {
        // Given
        val completedDelivery = testDelivery.copy(
            status = DeliveryStatus.COMPLETED,
            completedAt = java.time.LocalDateTime.now()
        )
        whenever(deliveryService.completeDelivery(1L)).thenReturn(completedDelivery)

        // When & Then
        mockMvc.perform(post("/api/deliveries/1/complete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    fun `존재하지 않는 배송 조회 시 404 반환 테스트`() {
        // Given
        whenever(deliveryService.getDeliveryById(999L)).thenReturn(null)

        // When & Then
        mockMvc.perform(get("/api/deliveries/999"))
            .andExpect(status().isNotFound)
    }
}