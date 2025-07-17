package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.exception.*
import com.logistics.repository.DeliveryRepository
import com.logistics.repository.DriverRepository
import com.logistics.dto.DeliveryRequestDto
import com.logistics.dto.DriverRevenueDto
import com.logistics.dto.DriverRevenueRequestDto
import com.logistics.constant.ErrorMessage
import com.logistics.constant.MessageUtils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) {
    
    fun getAllDeliveries(): List<Delivery> = deliveryRepository.findAll()
    
    fun getDeliveryById(id: Long): Delivery {
        return deliveryRepository.findById(id).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, id.toString()))
        }
    }
    
    fun createDelivery(dto: DeliveryRequestDto): Delivery {
        try {
            val driver = dto.driverId?.let { driverRepository.findById(it).orElse(null) }
            val delivery = Delivery(
                destination = dto.destination,
                address = dto.address,
                price = dto.price,
                feedTonnage = dto.toBigDecimal(), // Double을 BigDecimal로 변환
                deliveryDate = dto.deliveryDate,
                driver = driver,
                notes = dto.notes
            )
            validateDeliveryData(delivery)
            return deliveryRepository.save(delivery)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("배송 생성 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun createHistoricalDelivery(dto: DeliveryRequestDto): Delivery {
        try {
            val driver = dto.driverId?.let { driverRepository.findById(it).orElse(null) }
            val delivery = Delivery(
                destination = dto.destination,
                address = dto.address,
                price = dto.price,
                feedTonnage = dto.toBigDecimal(),
                deliveryDate = dto.deliveryDate,
                driver = driver,
                notes = dto.notes,
                status = DeliveryStatus.COMPLETED // 과거 데이터는 완료 상태로 생성
            )
            validateHistoricalDeliveryData(delivery)
            return deliveryRepository.save(delivery)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("과거 배송 데이터 생성 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun updateDelivery(id: Long, dto: DeliveryRequestDto): Delivery {
        if (!deliveryRepository.existsById(id)) {
            throw ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, id.toString()))
        }
        try {
            val driver = dto.driverId?.let { driverRepository.findById(it).orElse(null) }
            val delivery = Delivery(
                id = id,
                destination = dto.destination,
                address = dto.address,
                price = dto.price,
                feedTonnage = dto.toBigDecimal(), // Double을 BigDecimal로 변환
                deliveryDate = dto.deliveryDate,
                driver = driver,
                notes = dto.notes
            )
            validateDeliveryData(delivery)
            return deliveryRepository.save(delivery)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("배송 수정 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun deleteDelivery(id: Long) {
        val delivery = deliveryRepository.findById(id).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, id.toString()))
        }
        
        // 배송 상태 확인 - 진행 중인 배송은 삭제 불가
        if (delivery.status == DeliveryStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(ErrorMessage.DELIVERY_IN_PROGRESS_DELETE.message)
        }
        
        try {
            deliveryRepository.deleteById(id)
        } catch (e: Exception) {
            throw DataIntegrityException("배송 삭제 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    fun getPendingDeliveries(): List<Delivery> = deliveryRepository.findByStatus(DeliveryStatus.PENDING)
    
    fun getAssignedDeliveries(): List<Delivery> = 
        deliveryRepository.findByStatusIn(listOf(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_PROGRESS))
    
    /**
     * 공정한 배차를 위한 추천 알고리즘
     * 1. 휴가 중인 기사 제외
     * 2 최근 배송 횟수가 적은 기사 우선
     * 3은 지역 반복 배송 방지
     */
    fun recommendDriverForDelivery(deliveryId: Long): Driver? {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, deliveryId.toString()))
        }
        
        // 대기 상태가 아닌 배송은 추천 불가
        if (delivery.status != DeliveryStatus.PENDING) {
            throw BusinessRuleViolationException(ErrorMessage.DELIVERY_ALREADY_PROCESSED.message)
        }
        
        val availableDrivers = driverRepository.findAvailableDriversForDate(delivery.deliveryDate)
            .filter { it.status == DriverStatus.ACTIVE }
            .filter { it.tonnage >= delivery.feedTonnage.toDouble() }
        
        if (availableDrivers.isEmpty()) {
            return null
        }
        
        // 최근 배송 횟수가 적은 기사 우선 선택
        return availableDrivers.minByOrNull { driver ->
            val recentDeliveries = deliveryRepository.findDeliveriesByDriverAndDateRange(
                driver.id,
                LocalDate.now().minusDays(30),
                LocalDate.now()
            )
            recentDeliveries.size
        }
    }
    
    fun assignDeliveryToDriver(deliveryId: Long, driverId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, deliveryId.toString()))
        }
        
        val driver = driverRepository.findById(driverId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DRIVER_NOT_FOUND, driverId.toString()))
        }
        
        validateAssignment(delivery, driver)
        
        val updatedDelivery = delivery.copy(
            driver = driver,
            status = DeliveryStatus.ASSIGNED,
            assignedAt = java.time.LocalDateTime.now()
        )
        
        return try {
            deliveryRepository.save(updatedDelivery)
        } catch (e: Exception) {
            throw AssignmentException(MessageUtils.formatMessage(ErrorMessage.ASSIGNMENT_ERROR, e.message ?: "알 수 없는 오류"))
        }
    }
    
    fun startDelivery(deliveryId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, deliveryId.toString()))
        }
        
        // 배정된 배송만 시작 가능
        if (delivery.status != DeliveryStatus.ASSIGNED) {
            throw BusinessRuleViolationException(ErrorMessage.DELIVERY_NOT_ASSIGNED.message)
        }
        
        val updatedDelivery = delivery.copy(
            status = DeliveryStatus.IN_PROGRESS,
            startedAt = java.time.LocalDateTime.now()
        )
        
        return deliveryRepository.save(updatedDelivery)
    }
    
    fun completeDelivery(deliveryId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, deliveryId.toString()))
        }
        
        // 완료 가능 상태 검증
        if (delivery.status != DeliveryStatus.ASSIGNED && delivery.status != DeliveryStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(ErrorMessage.DELIVERY_NOT_ASSIGNED.message)
        }
        
        val updatedDelivery = delivery.copy(
            status = DeliveryStatus.COMPLETED,
            completedAt = java.time.LocalDateTime.now()
        )
        
        return deliveryRepository.save(updatedDelivery)
    }
    
    fun cancelAssignment(deliveryId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DELIVERY_NOT_FOUND, deliveryId.toString()))
        }
        
        // 배차 취소 가능 상태 검증
        if (delivery.status != DeliveryStatus.ASSIGNED && delivery.status != DeliveryStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException("배차 취소는 배차 완료 또는 진행 중인 배송만 가능합니다.")
        }
        
        val updatedDelivery = delivery.copy(
            driver = null,
            status = DeliveryStatus.PENDING,
            assignedAt = null,
            startedAt = null
        )
        
        return deliveryRepository.save(updatedDelivery)
    }
    
    /**
     * 기사별 월매출표 조회
     */
    fun getDriverRevenue(request: DriverRevenueRequestDto): List<DriverRevenueDto> {
        val yearMonth = request.yearMonth
        
        return if (request.driverId != null) {
            // 특정 기사의 월매출표
            val driver = driverRepository.findById(request.driverId).orElseThrow {
                ResourceNotFoundException(MessageUtils.formatMessage(ErrorMessage.DRIVER_NOT_FOUND, request.driverId.toString()))
            }
            
            val completedDeliveries = deliveryRepository.findCompletedDeliveriesByDriverAndYearMonth(request.driverId, yearMonth)
            listOf(calculateDriverRevenue(driver, yearMonth, completedDeliveries))
        } else {
            // 모든 기사의 월매출표
            val allDrivers = driverRepository.findAll()
            val allCompletedDeliveries = deliveryRepository.findCompletedDeliveriesByYearMonth(yearMonth)
            
            allDrivers.map { driver ->
                val driverDeliveries = allCompletedDeliveries.filter { it.driver?.id == driver.id }
                calculateDriverRevenue(driver, yearMonth, driverDeliveries)
            }
        }
    }
    
    /**
     * 배송 데이터 검증
     */
    private fun validateDeliveryData(delivery: Delivery) {
        if (delivery.destination.isBlank()) {
            throw InvalidRequestException(ErrorMessage.DESTINATION_REQUIRED.message)
        }
        
        if (delivery.address.isBlank()) {
            throw InvalidRequestException(ErrorMessage.ADDRESS_REQUIRED.message)
        }
        
        if (delivery.price.signum() <= 0) {
            throw InvalidRequestException(ErrorMessage.PRICE_POSITIVE.message)
        }
        
        if (delivery.feedTonnage.signum() <= 0) {
            throw InvalidRequestException(ErrorMessage.FEED_TONNAGE_POSITIVE.message)
        }
        
        if (delivery.deliveryDate.isBefore(LocalDate.now())) {
            throw InvalidRequestException(ErrorMessage.DELIVERY_DATE_FUTURE.message)
        }
    }
    
    /**
     * 과거 배송 데이터 검증 (날짜 검증 제외)
     */
    private fun validateHistoricalDeliveryData(delivery: Delivery) {
        if (delivery.destination.isBlank()) {
            throw InvalidRequestException(ErrorMessage.DESTINATION_REQUIRED.message)
        }
        
        if (delivery.address.isBlank()) {
            throw InvalidRequestException(ErrorMessage.ADDRESS_REQUIRED.message)
        }
        
        if (delivery.price.signum() <= 0) {
            throw InvalidRequestException(ErrorMessage.PRICE_POSITIVE.message)
        }
        
        if (delivery.feedTonnage.signum() <= 0) {
            throw InvalidRequestException(ErrorMessage.FEED_TONNAGE_POSITIVE.message)
        }
        
        // 과거 데이터는 날짜 검증을 하지 않음
    }
    
    /**
     * 배차 가능 여부 검증
     */
    private fun validateAssignment(delivery: Delivery, driver: Driver) {
        // 이미 배정된 배송인지 확인
        if (delivery.status != DeliveryStatus.PENDING) {
            throw BusinessRuleViolationException(ErrorMessage.DELIVERY_ALREADY_PROCESSED.message)
        }
        
        // 기사 상태 확인
        if (driver.status != DriverStatus.ACTIVE) {
            throw BusinessRuleViolationException(ErrorMessage.DRIVER_NOT_ACTIVE.message)
        }
        
        // 기사의 차량 톤수와 사료량 비교
        if (driver.tonnage < delivery.feedTonnage.toDouble()) {
            val params = mapOf(
                "driverTonnage" to driver.tonnage.toString(),
                "feedTonnage" to delivery.feedTonnage.toString()
            )
            throw BusinessRuleViolationException(MessageUtils.formatMessage(ErrorMessage.DRIVER_TONNAGE_INSUFFICIENT, params))
        }
        
        // 휴가 중인지 확인
        val availableDrivers = driverRepository.findAvailableDriversForDate(delivery.deliveryDate)
        if (!availableDrivers.contains(driver)) {
            throw BusinessRuleViolationException(ErrorMessage.DRIVER_NOT_AVAILABLE.message)
        }
    }
    
    /**
     * 기사별 매출 계산
     */
    private fun calculateDriverRevenue(driver: Driver, yearMonth: YearMonth, deliveries: List<Delivery>): DriverRevenueDto {
        val totalDeliveries = deliveries.size
        val totalRevenue = deliveries.sumOf { it.price }
        val totalTonnage = deliveries.sumOf { it.feedTonnage }
        val averageRevenuePerDelivery = if (totalDeliveries > 0) {
            totalRevenue.divide(BigDecimal(totalDeliveries),2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        
        return DriverRevenueDto(
            driverId = driver.id,
            driverName = driver.name,
            yearMonth = yearMonth,
            totalDeliveries = totalDeliveries,
            totalRevenue = totalRevenue,
            totalTonnage = totalTonnage,
            averageRevenuePerDelivery = averageRevenuePerDelivery
        )
    }
}