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
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@Import(QuerydslConfig::class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
        // 각 테스트마다 고유한 phoneNumber 사용
        val uniqueSuffix = System.currentTimeMillis() % 10000
        testDriver = Driver(
            name = "김기사",
            phoneNumber = "010-1234-${String.format("%04d", uniqueSuffix)}",
            vehicleNumber = "12가${String.format("%04d", uniqueSuffix)}",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        testDriver = entityManager.merge(testDriver)
        entityManager.flush()

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
        val pendingDriver = Driver(
            name = "김기사",
            phoneNumber = "010-0000-0001",
            vehicleNumber = "11가1111",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        val persistedPendingDriver = entityManager.merge(pendingDriver)
        val pendingDelivery = testDelivery.copy(status = DeliveryStatus.PENDING, driver = persistedPendingDriver)

        val completedDriver = Driver(
            name = "이기사",
            phoneNumber = "010-0000-0002",
            vehicleNumber = "22나2222",
            vehicleType = "트럭",
            tonnage = 3.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(3)
        )
        val persistedCompletedDriver = entityManager.merge(completedDriver)
        val completedDelivery = testDelivery.copy(
            destination = "부산농장",
            status = DeliveryStatus.COMPLETED,
            driver = persistedCompletedDriver
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
        val driver = Driver(
            name = "김기사",
            phoneNumber = "010-0000-0003",
            vehicleNumber = "33다3333",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        val persistedDriver = entityManager.merge(driver)
        val delivery1 = testDelivery.copy(destination = "서울농장", driver = persistedDriver)
        val delivery2 = testDelivery.copy(destination = "인천농장", driver = persistedDriver)
        entityManager.persist(delivery1)
        entityManager.persist(delivery2)
        entityManager.flush()

        // When
        val driverDeliveries = deliveryRepository.findByDriverId(persistedDriver.id)

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
        val driver1 = Driver(
            name = "김기사",
            phoneNumber = "010-0000-0004",
            vehicleNumber = "44라4444",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        val persistedDriver1 = entityManager.merge(driver1)
        val delivery1 = testDelivery.copy(deliveryDate = tomorrow, driver = persistedDriver1)
        val driver2 = Driver(
            name = "박기사",
            phoneNumber = "010-0000-0005",
            vehicleNumber = "55마5555",
            vehicleType = "트럭",
            tonnage = 4.5,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(2)
        )
        val persistedDriver2 = entityManager.merge(driver2)
        val delivery2 = testDelivery.copy(
            destination = "부산농장",
            deliveryDate = dayAfterTomorrow,
            driver = persistedDriver2
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
        val driver = Driver(
            name = "김기사",
            phoneNumber = "010-0000-0006",
            vehicleNumber = "66바6666",
            vehicleType = "트럭",
            tonnage = 5.0,
            status = DriverStatus.ACTIVE,
            joinDate = LocalDate.now().minusMonths(6)
        )
        val persistedDriver = entityManager.merge(driver)
        val delivery = testDelivery.copy(driver = persistedDriver)
        val savedDelivery = deliveryRepository.save(delivery)
        val foundDelivery = deliveryRepository.findById(savedDelivery.id)

        // Then
        assertTrue(foundDelivery.isPresent)
        assertEquals("서울농장", foundDelivery.get().destination)
        assertEquals(BigDecimal("3.5"), foundDelivery.get().feedTonnage)
        assertEquals(BigDecimal("500000"), foundDelivery.get().price)
        assertEquals(persistedDriver.id, foundDelivery.get().driver?.id)
    }
}