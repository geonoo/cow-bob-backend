package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.exception.*
import com.logistics.repository.DeliveryRepository
import com.logistics.repository.DriverRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) {
    
    fun getAllDeliveries(): List<Delivery> = deliveryRepository.findAll()
    
    fun getDeliveryById(id: Long): Delivery {
        return deliveryRepository.findById(id).orElseThrow {
            ResourceNotFoundException("ID가 $id 인 배송을 찾을 수 없습니다.")
        }
    }
    
    fun createDelivery(delivery: Delivery): Delivery {
        try {
            // 비즈니스 규칙 검증
            validateDeliveryData(delivery)
            return deliveryRepository.save(delivery)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("배송 생성 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun updateDelivery(id: Long, updatedDelivery: Delivery): Delivery {
        if (!deliveryRepository.existsById(id)) {
            throw ResourceNotFoundException("ID가 $id 인 배송을 찾을 수 없습니다.")
        }
        
        try {
            validateDeliveryData(updatedDelivery)
            return deliveryRepository.save(updatedDelivery.copy(id = id))
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("배송 수정 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun deleteDelivery(id: Long) {
        val delivery = deliveryRepository.findById(id).orElseThrow {
            ResourceNotFoundException("ID가 $id 인 배송을 찾을 수 없습니다.")
        }
        
        // 배송 상태 확인 - 진행 중인 배송은 삭제 불가
        if (delivery.status == DeliveryStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException("진행 중인 배송은 삭제할 수 없습니다.")
        }
        
        try {
            deliveryRepository.deleteById(id)
        } catch (e: Exception) {
            throw DataIntegrityException("배송 삭제 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    fun getPendingDeliveries(): List<Delivery> = deliveryRepository.findByStatus(DeliveryStatus.PENDING)
    
    /**
     * 공정한 배차를 위한 추천 알고리즘
     * 1. 휴가 중인 기사 제외
     * 2. 최근 배송 횟수가 적은 기사 우선
     * 3. 같은 지역 반복 배송 방지
     */
    fun recommendDriverForDelivery(delivery: Delivery): Driver? {
        val availableDrivers = driverRepository.findAvailableDriversForDate(delivery.deliveryDate)
        
        if (availableDrivers.isEmpty()) return null
        
        // 각 기사별 점수 계산 (낮을수록 우선순위 높음)
        val driverScores = availableDrivers.map { driver ->
            val totalDeliveries = driver.deliveries.size
            val recentDeliveries = driver.deliveries.count { 
                it.deliveryDate.isAfter(LocalDate.now().minusDays(30)) 
            }
            val sameDestinationCount = deliveryRepository.countRecentDeliveriesForDriverAndDestination(
                driver.id, delivery.destination, LocalDate.now().minusDays(30)
            )
            
            // 점수 계산: 총 배송 + (최근 배송 * 2) + (같은 지역 배송 * 5)
            val score = totalDeliveries + (recentDeliveries * 2) + (sameDestinationCount * 5).toInt()
            
            driver to score
        }
        
        // 점수가 가장 낮은 기사 선택
        return driverScores.minByOrNull { it.second }?.first
    }
    
    fun assignDeliveryToDriver(deliveryId: Long, driverId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException("ID가 $deliveryId 인 배송을 찾을 수 없습니다.")
        }
        
        val driver = driverRepository.findById(driverId).orElseThrow {
            ResourceNotFoundException("ID가 $driverId 인 기사를 찾을 수 없습니다.")
        }
        
        // 배차 가능 여부 검증
        validateAssignment(delivery, driver)
        
        try {
            val updatedDelivery = delivery.copy(
                driver = driver,
                status = DeliveryStatus.ASSIGNED
            )
            
            return deliveryRepository.save(updatedDelivery)
        } catch (e: Exception) {
            throw AssignmentException("배차 처리 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    fun completeDelivery(deliveryId: Long): Delivery {
        val delivery = deliveryRepository.findById(deliveryId).orElseThrow {
            ResourceNotFoundException("ID가 $deliveryId 인 배송을 찾을 수 없습니다.")
        }
        
        // 완료 가능 상태 검증
        if (delivery.status != DeliveryStatus.ASSIGNED && delivery.status != DeliveryStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException("배정되지 않은 배송은 완료할 수 없습니다.")
        }
        
        try {
            val updatedDelivery = delivery.copy(
                status = DeliveryStatus.COMPLETED,
                completedAt = java.time.LocalDateTime.now()
            )
            
            return deliveryRepository.save(updatedDelivery)
        } catch (e: Exception) {
            throw DataIntegrityException("배송 완료 처리 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    /**
     * 배송 데이터 검증
     */
    private fun validateDeliveryData(delivery: Delivery) {
        if (delivery.destination.isBlank()) {
            throw InvalidRequestException("배송지는 필수 입력 항목입니다.")
        }
        
        if (delivery.address.isBlank()) {
            throw InvalidRequestException("주소는 필수 입력 항목입니다.")
        }
        
        if (delivery.price.signum() <= 0) {
            throw InvalidRequestException("가격은 0보다 커야 합니다.")
        }
        
        if (delivery.feedTonnage.signum() <= 0) {
            throw InvalidRequestException("사료량은 0보다 커야 합니다.")
        }
        
        if (delivery.deliveryDate.isBefore(LocalDate.now())) {
            throw InvalidRequestException("배송일은 오늘 이후여야 합니다.")
        }
    }
    
    /**
     * 배차 가능 여부 검증
     */
    private fun validateAssignment(delivery: Delivery, driver: Driver) {
        // 이미 배정된 배송인지 확인
        if (delivery.status != DeliveryStatus.PENDING) {
            throw BusinessRuleViolationException("이미 처리된 배송입니다.")
        }
        
        // 기사 상태 확인
        if (driver.status != DriverStatus.ACTIVE) {
            throw BusinessRuleViolationException("활성 상태가 아닌 기사에게는 배차할 수 없습니다.")
        }
        
        // 기사의 차량 톤수와 사료량 비교
        if (driver.tonnage < delivery.feedTonnage.toDouble()) {
            throw BusinessRuleViolationException("기사의 차량 톤수(${driver.tonnage}톤)가 사료량(${delivery.feedTonnage}톤)보다 작습니다.")
        }
        
        // 휴가 중인지 확인
        val availableDrivers = driverRepository.findAvailableDriversForDate(delivery.deliveryDate)
        if (!availableDrivers.contains(driver)) {
            throw BusinessRuleViolationException("해당 날짜에 기사가 사용 가능하지 않습니다.")
        }
    }
}