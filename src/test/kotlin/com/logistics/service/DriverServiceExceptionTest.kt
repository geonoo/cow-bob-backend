package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.exception.*
import com.logistics.repository.DriverRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class DriverServiceExceptionTest {

    @Mock
    private lateinit var driverRepository: DriverRepository

    @InjectMocks
    private lateinit var driverService: DriverService

    private lateinit var validDriver: Driver
    private lateinit var driverWithDeliveries: Driver

    @BeforeEach
    fun setUp() {
        validDriver = Driver(
            id = 1L,
            name = "김기사",
            phoneNumber = "010-1234-5678",
            vehicleNumber = "12가3456",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )

        val activeDelivery = Delivery(
            id = 1L,
            destination = "서울농장",
            address = "서울시 강남구",
            price = BigDecimal("500000"),
            feedTonnage = BigDecimal("3.5"),
            deliveryDate = LocalDate.now().plusDays(1),
            status = DeliveryStatus.ASSIGNED
        )

        driverWithDeliveries = validDriver.copy(
            id = 2L,
            deliveries = listOf(activeDelivery)
        )
    }

    // ========== ResourceNotFoundException 테스트 ==========

    @Test
    fun `존재하지 않는 기사 조회 시 ResourceNotFoundException 발생`() {
        // Given
        whenever(driverRepository.findById(999L)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            driverService.getDriverById(999L)
        }
        assertEquals("ID가 999 인 기사를 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `존재하지 않는 기사 수정 시 ResourceNotFoundException 발생`() {
        // Given
        whenever(driverRepository.existsById(999L)).thenReturn(false)

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            driverService.updateDriver(999L, validDriver)
        }
        assertEquals("ID가 999 인 기사를 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `존재하지 않는 기사 삭제 시 ResourceNotFoundException 발생`() {
        // Given
        whenever(driverRepository.findById(999L)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            driverService.deleteDriver(999L)
        }
        assertEquals("ID가 999 인 기사를 찾을 수 없습니다.", exception.message)
    }

    // ========== InvalidRequestException 테스트 ==========

    @Test
    fun `빈 이름으로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(name = "")

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("기사 이름은 필수 입력 항목입니다.", exception.message)
    }

    @Test
    fun `빈 전화번호로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(phoneNumber = "")

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("전화번호는 필수 입력 항목입니다.", exception.message)
    }

    @Test
    fun `잘못된 전화번호 형식으로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(phoneNumber = "010-12345-678") // 잘못된 형식

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("올바른 전화번호 형식이 아닙니다.", exception.message)
    }

    @Test
    fun `빈 차량번호로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(vehicleNumber = "")

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("차량번호는 필수 입력 항목입니다.", exception.message)
    }

    @Test
    fun `빈 차량종류로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(vehicleType = "")

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("차량종류는 필수 입력 항목입니다.", exception.message)
    }

    @Test
    fun `음수 톤수로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(tonnage = -1.0)

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("톤수는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `미래 가입일로 기사 생성 시 InvalidRequestException 발생`() {
        // Given
        val invalidDriver = validDriver.copy(joinDate = LocalDate.now().plusDays(1))

        // When & Then
        val exception = assertThrows<InvalidRequestException> {
            driverService.createDriver(invalidDriver)
        }
        assertEquals("가입일은 오늘 이전이어야 합니다.", exception.message)
    }

    // ========== BusinessRuleViolationException 테스트 ==========

    @Test
    fun `중복된 전화번호로 기사 생성 시 BusinessRuleViolationException 발생`() {
        // Given
        val existingDriver = validDriver.copy(id = 2L)
        whenever(driverRepository.findAll()).thenReturn(listOf(existingDriver))

        // When & Then
        val exception = assertThrows<BusinessRuleViolationException> {
            driverService.createDriver(validDriver)
        }
        assertEquals("이미 등록된 전화번호입니다.", exception.message)
    }

    @Test
    fun `중복된 전화번호로 기사 수정 시 BusinessRuleViolationException 발생`() {
        // Given
        val existingDriver = validDriver.copy(id = 2L, phoneNumber = "010-9999-9999")
        val anotherDriver = validDriver.copy(id = 3L, phoneNumber = "010-1234-5678")
        
        whenever(driverRepository.existsById(1L)).thenReturn(true)
        whenever(driverRepository.findAll()).thenReturn(listOf(existingDriver, anotherDriver))

        val updatedDriver = validDriver.copy(phoneNumber = "010-1234-5678") // 다른 기사와 중복

        // When & Then
        val exception = assertThrows<BusinessRuleViolationException> {
            driverService.updateDriver(1L, updatedDriver)
        }
        assertEquals("이미 등록된 전화번호입니다.", exception.message)
    }

    @Test
    fun `배송 중인 기사 삭제 시 BusinessRuleViolationException 발생`() {
        // Given
        whenever(driverRepository.findById(2L)).thenReturn(Optional.of(driverWithDeliveries))

        // When & Then
        val exception = assertThrows<BusinessRuleViolationException> {
            driverService.deleteDriver(2L)
        }
        assertEquals("배송 중인 기사는 삭제할 수 없습니다.", exception.message)
    }

    // ========== DataIntegrityException 테스트 ==========

    @Test
    fun `기사 생성 중 데이터베이스 오류 시 DataIntegrityException 발생`() {
        // Given
        whenever(driverRepository.findAll()).thenReturn(emptyList())
        whenever(driverRepository.save(any<Driver>())).thenThrow(RuntimeException("DB 연결 오류"))

        // When & Then
        val exception = assertThrows<DataIntegrityException> {
            driverService.createDriver(validDriver)
        }
        assertTrue(exception.message!!.contains("기사 생성 중 오류가 발생했습니다"))
    }

    @Test
    fun `기사 수정 중 데이터베이스 오류 시 DataIntegrityException 발생`() {
        // Given
        whenever(driverRepository.existsById(1L)).thenReturn(true)
        whenever(driverRepository.findAll()).thenReturn(emptyList())
        whenever(driverRepository.save(any<Driver>())).thenThrow(RuntimeException("DB 연결 오류"))

        // When & Then
        val exception = assertThrows<DataIntegrityException> {
            driverService.updateDriver(1L, validDriver)
        }
        assertTrue(exception.message!!.contains("기사 수정 중 오류가 발생했습니다"))
    }

    @Test
    fun `기사 삭제 중 데이터베이스 오류 시 DataIntegrityException 발생`() {
        // Given
        val driverWithoutDeliveries = validDriver.copy(deliveries = emptyList())
        whenever(driverRepository.findById(1L)).thenReturn(Optional.of(driverWithoutDeliveries))
        whenever(driverRepository.deleteById(1L)).thenThrow(RuntimeException("DB 연결 오류"))

        // When & Then
        val exception = assertThrows<DataIntegrityException> {
            driverService.deleteDriver(1L)
        }
        assertTrue(exception.message!!.contains("기사 삭제 중 오류가 발생했습니다"))
    }

    // ========== 전화번호 형식 검증 테스트 ==========

    @Test
    fun `다양한 잘못된 전화번호 형식 테스트`() {
        val invalidPhoneNumbers = listOf(
            "010-123-4567",      // 중간 자리수 부족
            "010-12345-6789",    // 중간 자리수 초과
            "010-1234-567",      // 마지막 자리수 부족
            "010-1234-56789",    // 마지막 자리수 초과
            "02-1234-5678",      // 잘못된 시작 번호
            "010 1234 5678",     // 공백 구분자
            "010.1234.5678",     // 점 구분자
            "01012345678",       // 구분자 없음
            "010-abcd-efgh",     // 문자 포함
            ""                   // 빈 문자열
        )

        invalidPhoneNumbers.forEach { phoneNumber ->
            val invalidDriver = validDriver.copy(phoneNumber = phoneNumber)
            
            val exception = assertThrows<InvalidRequestException> {
                driverService.createDriver(invalidDriver)
            }
            
            assertTrue(
                exception.message == "전화번호는 필수 입력 항목입니다." || 
                exception.message == "올바른 전화번호 형식이 아닙니다.",
                "전화번호 '$phoneNumber'에 대한 예외 메시지가 예상과 다릅니다: ${exception.message}"
            )
        }
    }

    @Test
    fun `올바른 전화번호 형식 테스트`() {
        // Given
        val validPhoneNumbers = listOf(
            "010-1234-5678",
            "011-9876-5432",
            "016-1111-2222",
            "017-3333-4444",
            "018-5555-6666",
            "019-7777-8888"
        )

        validPhoneNumbers.forEach { phoneNumber ->
            val driver = validDriver.copy(phoneNumber = phoneNumber)
            whenever(driverRepository.findAll()).thenReturn(emptyList())
            whenever(driverRepository.save(any<Driver>())).thenReturn(driver)

            // When & Then - 예외가 발생하지 않아야 함
            val result = driverService.createDriver(driver)
            assertEquals(phoneNumber, result.phoneNumber)
        }
    }
}