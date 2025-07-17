package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.repository.DeliveryRepository
import com.logistics.repository.DriverRepository
import com.logistics.dto.DeliveryRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class DeliveryServiceTest {

    @Mock
    private lateinit var deliveryRepository: DeliveryRepository

    @Mock
    private lateinit var driverRepository: DriverRepository

    @InjectMocks
    private lateinit var deliveryService: DeliveryService

    private lateinit var testDeliveryDto: DeliveryRequestDto
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

        testDeliveryDto = DeliveryRequestDto(
            destination = "서울농장",
            address = "서울시 강남구 테헤란로 123",
            price = BigDecimal("500000"),
            feedTonnage = 3.5,
            deliveryDate = LocalDate.now().plusDays(1)
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
    fun `모든 배송 조회 테스트`() {
        // Given
        val deliveries = listOf(testDelivery)
        whenever(deliveryRepository.findAll()).thenReturn(deliveries)

        // When
        val result = deliveryService.getAllDeliveries()

        // Then
        assertEquals(1, result.size)
        assertEquals(testDelivery.destination, result[0].destination)
        verify(deliveryRepository).findAll()
    }

    @Test
    fun `배송 생성 테스트`() {
        // Given
        whenever(deliveryRepository.save(any<Delivery>())).thenReturn(testDelivery)

        // When
        val result = deliveryService.createDelivery(testDeliveryDto)

        // Then
        assertNotNull(result)
        assertEquals(testDelivery.destination, result.destination)
        assertEquals(testDelivery.feedTonnage, result.feedTonnage)
        verify(deliveryRepository).save(any<Delivery>())
    }

    @Test
    fun `ID로 배송 조회 테스트`() {
        // Given
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery))

        // When
        val result = deliveryService.getDeliveryById(1L)

        // Then
        assertNotNull(result)
        assertEquals(testDelivery.destination, result.destination)
        verify(deliveryRepository).findById(1L)
    }

    @Test
    fun `배송 수정 테스트`() {
        // Given
        whenever(deliveryRepository.existsById(1L)).thenReturn(true)
        whenever(deliveryRepository.save(any<Delivery>())).thenReturn(testDelivery)

        // When
        val result = deliveryService.updateDelivery(1L, testDeliveryDto)

        // Then
        assertNotNull(result)
        assertEquals(testDelivery.destination, result.destination)
        verify(deliveryRepository).save(any<Delivery>())
    }

    @Test
    fun `배송 삭제 테스트`() {
        // Given
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery))

        // When
        deliveryService.deleteDelivery(1L)

        // Then
        verify(deliveryRepository).deleteById(1L)
    }

    @Test
    fun `대기 중인 배송 조회 테스트`() {
        // Given
        val pendingDeliveries = listOf(testDelivery)
        whenever(deliveryRepository.findByStatus(DeliveryStatus.PENDING)).thenReturn(pendingDeliveries)

        // When
        val result = deliveryService.getPendingDeliveries()

        // Then
        assertEquals(1, result.size)
        assertEquals(DeliveryStatus.PENDING, result[0].status)
        verify(deliveryRepository).findByStatus(DeliveryStatus.PENDING)
    }

    @Test
    fun `기사 추천 테스트`() {
        // Given
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery))
        whenever(driverRepository.findAvailableDriversForDate(testDelivery.deliveryDate))
            .thenReturn(listOf(testDriver))

        // When
        val result = deliveryService.recommendDriverForDelivery(1L)

        // Then
        assertNotNull(result)
        assertEquals(testDriver.id, result!!.id)
        verify(deliveryRepository).findById(1L)
    }

    @Test
    fun `배송 완료 테스트`() {
        // Given
        val assignedDelivery = testDelivery.copy(status = DeliveryStatus.ASSIGNED)
        val completedDelivery = assignedDelivery.copy(status = DeliveryStatus.COMPLETED)
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(assignedDelivery))
        whenever(deliveryRepository.save(any<Delivery>())).thenReturn(completedDelivery)

        // When
        val result = deliveryService.completeDelivery(1L)

        // Then
        assertNotNull(result)
        assertEquals(DeliveryStatus.COMPLETED, result.status)
        verify(deliveryRepository).save(any<Delivery>())
    }
}