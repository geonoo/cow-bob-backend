package com.logistics.service

import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.repository.DriverRepository
import com.logistics.dto.DriverRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class DriverServiceTest {

    @Mock
    private lateinit var driverRepository: DriverRepository

    @InjectMocks
    private lateinit var driverService: DriverService

    private lateinit var testDriverDto: DriverRequestDto
    private lateinit var testDriver: Driver

    @BeforeEach
    fun setUp() {
        testDriverDto = DriverRequestDto(
            name = "김기사",
            phoneNumber = "010-1234-5678",
            vehicleNumber = "12가3456",
            vehicleType = "트럭",
            tonnage = 5.0
        )

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
    }

    @Test
    fun `모든 기사 조회 테스트`() {
        // Given
        val drivers = listOf(testDriver)
        whenever(driverRepository.findAll()).thenReturn(drivers)

        // When
        val result = driverService.getAllDrivers()

        // Then
        assertEquals(1, result.size)
        assertEquals(testDriver.name, result[0].name)
        verify(driverRepository).findAll()
    }

    @Test
    fun `기사 생성 테스트`() {
        // Given
        whenever(driverRepository.findAll()).thenReturn(emptyList()) // 중복 전화번호 검증을 위해
        whenever(driverRepository.save(any<Driver>())).thenReturn(testDriver)

        // When
        val result = driverService.createDriver(testDriverDto)

        // Then
        assertNotNull(result)
        assertEquals(testDriver.name, result.name)
        assertEquals(testDriver.phoneNumber, result.phoneNumber)
        assertEquals(testDriver.vehicleNumber, result.vehicleNumber)
        verify(driverRepository).save(any<Driver>())
    }

    @Test
    fun `ID로 기사 조회 테스트`() {
        // Given
        whenever(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver))

        // When
        val result = driverService.getDriverById(1L)

        // Then
        assertNotNull(result)
        assertEquals(testDriver.name, result.name)
        verify(driverRepository).findById(1L)
    }

    @Test
    fun `활성 기사 조회 테스트`() {
        // Given
        val activeDrivers = listOf(testDriver)
        whenever(driverRepository.findByStatus(DriverStatus.ACTIVE)).thenReturn(activeDrivers)

        // When
        val result = driverService.getActiveDrivers()

        // Then
        assertEquals(1, result.size)
        assertEquals(DriverStatus.ACTIVE, result[0].status)
        verify(driverRepository).findByStatus(DriverStatus.ACTIVE)
    }

    @Test
    fun `기사 수정 테스트`() {
        // Given
        whenever(driverRepository.existsById(1L)).thenReturn(true)
        whenever(driverRepository.findAll()).thenReturn(emptyList()) // 중복 전화번호 검증을 위해
        whenever(driverRepository.save(any<Driver>())).thenReturn(testDriver)

        // When
        val result = driverService.updateDriver(1L, testDriverDto)

        // Then
        assertNotNull(result)
        assertEquals(testDriver.name, result.name)
        verify(driverRepository).save(any<Driver>())
    }

    @Test
    fun `기사 삭제 테스트`() {
        // Given
        val driverWithoutDeliveries = testDriver.copy(deliveries = emptyList())
        whenever(driverRepository.findById(1L)).thenReturn(Optional.of(driverWithoutDeliveries))

        // When
        driverService.deleteDriver(1L)

        // Then
        verify(driverRepository).deleteById(1L)
    }
}