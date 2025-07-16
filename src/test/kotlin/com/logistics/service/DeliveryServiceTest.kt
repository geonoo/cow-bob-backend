package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.repository.DeliveryRepository
import com.logistics.repository.DriverRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val result = deliveryService.createDelivery(testDelivery)

        // Then
        assertNotNull(result)
        assertEquals(testDelivery.destination, result.destination)
        assertEquals(testDelivery.feedTonnage, result.feedTonnage)
        verify(deliveryRepository).save(testDelivery)
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
    fun `배차 할당 테스트`() {
        // Given
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery))
        whenever(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver))
        
        val assignedDelivery = testDelivery.copy(
            driver = testDriver,
            status = DeliveryStatus.ASSIGNED
        )
        whenever(deliveryRepository.save(any<Delivery>())).thenReturn(assignedDelivery)

        // When
        val result = deliveryService.assignDeliveryToDriver(1L, 1L)

        // Then
        assertNotNull(result)
        assertEquals(testDriver, result.driver)
        assertEquals(DeliveryStatus.ASSIGNED, result.status)
        verify(deliveryRepository).findById(1L)
        verify(driverRepository).findById(1L)
        verify(deliveryRepository).save(any<Delivery>())
    }

    @Test
    fun `존재하지 않는 배송 할당 시 null 반환 테스트`() {
        // Given
        whenever(deliveryRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = deliveryService.assignDeliveryToDriver(999L, 1L)

        // Then
        assertNull(result)
        verify(deliveryRepository).findById(999L)
    }

    @Test
    fun `배송 완료 처리 테스트`() {
        // Given
        whenever(deliveryRepository.findById(1L)).thenReturn(Optional.of(testDelivery))
        
        val completedDelivery = testDelivery.copy(
            status = DeliveryStatus.COMPLETED,
            completedAt = java.time.LocalDateTime.now()
        )
        whenever(deliveryRepository.save(any<Delivery>())).thenReturn(completedDelivery)

        // When
        val result = deliveryService.completeDelivery(1L)

        // Then
        assertNotNull(result)
        assertEquals(DeliveryStatus.COMPLETED, result.status)
        assertNotNull(result.completedAt)
        verify(deliveryRepository).findById(1L)
        verify(deliveryRepository).save(any<Delivery>())
    }
}