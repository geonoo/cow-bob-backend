package com.logistics.repository

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.config.QuerydslConfig
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import jakarta.persistence.EntityManager
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(QuerydslConfig::class)
@Transactional
class DeliveryRepositoryTest {

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var deliveryRepository: DeliveryRepository

    @Autowired
    private lateinit var driverRepository: DriverRepository

    private lateinit var testDriver: Driver
    private lateinit var testDelivery: Delivery

    @BeforeEach
    fun setUp() {
        testDriver = Driver(
            name = "김기사",
            phoneNumber = "010-1234-5678",
            vehicleNumber = "12가3456",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        
        entityManager.persist(testDriver)
        entityManager.flush()
        testDriver = testDriver

        testDelivery = Delivery(
            destination = "서울농장",
            address = "서울시 강남구 테헤란로 123",
            price = BigDecimal("500000"),
            feedTonnage = BigDecimal("3.5"),
            deliveryDate = LocalDate.now().plusDays(1),
            driver = testDriver,
            status = DeliveryStatus.PENDING
        )
    }

    @Test
    fun `상태별 배송 조회 테스트`() {
        // Given
        val pendingDelivery = testDelivery.copy(status = DeliveryStatus.PENDING)
        val completedDelivery = testDelivery.copy(
            destination = "부산농장",
            status = DeliveryStatus.COMPLETED
        )
        
        entityManager.persist(pendingDelivery)
        entityManager.persist(completedDelivery)
        entityManager.flush()

        // When
        val pendingDeliveries = deliveryRepository.findByStatus(DeliveryStatus.PENDING)
        val completedDeliveries = deliveryRepository.findByStatus(DeliveryStatus.COMPLETED)

        // Then
        assertEquals(1, pendingDeliveries.size)
        assertEquals(1, completedDeliveries.size)
        assertEquals("서울농장", pendingDeliveries[0].destination)
        assertEquals("부산농장", completedDeliveries[0].destination)
    }

    @Test
    fun `기사별 배송 조회 테스트`() {
        // Given
        val delivery1 = testDelivery.copy(destination = "서울농장")
        val delivery2 = testDelivery.copy(destination = "인천농장")
        
        entityManager.persist(delivery1)
        entityManager.persist(delivery2)
        entityManager.flush()

        // When
        val driverDeliveries = deliveryRepository.findByDriverId(testDriver.id)

        // Then
        assertEquals(2, driverDeliveries.size)
        assertTrue(driverDeliveries.any { it.destination == "서울농장" })
        assertTrue(driverDeliveries.any { it.destination == "인천농장" })
    }

    @Test
    fun `날짜 범위별 배송 조회 테스트`() {
        // Given
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val dayAfterTomorrow = today.plusDays(2)

        val delivery1 = testDelivery.copy(deliveryDate = tomorrow)
        val delivery2 = testDelivery.copy(
            destination = "부산농장",
            deliveryDate = dayAfterTomorrow
        )
        
        entityManager.persist(delivery1)
        entityManager.persist(delivery2)
        entityManager.flush()

        // When
        val deliveriesInRange = deliveryRepository.findByDeliveryDateBetween(tomorrow, dayAfterTomorrow)

        // Then
        assertEquals(2, deliveriesInRange.size)
    }

    @Test
    fun `배송 저장 및 조회 테스트`() {
        // Given & When
        val savedDelivery = deliveryRepository.save(testDelivery)
        val foundDelivery = deliveryRepository.findById(savedDelivery.id)

        // Then
        assertTrue(foundDelivery.isPresent)
        assertEquals("서울농장", foundDelivery.get().destination)
        assertEquals(BigDecimal("3.5"), foundDelivery.get().feedTonnage)
        assertEquals(BigDecimal("500000"), foundDelivery.get().price)
        assertEquals(testDriver.id, foundDelivery.get().driver?.id)
    }
}