package com.logistics.service

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.Driver
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
    
    fun getDeliveryById(id: Long): Delivery? = deliveryRepository.findById(id).orElse(null)
    
    fun createDelivery(delivery: Delivery): Delivery = deliveryRepository.save(delivery)
    
    fun updateDelivery(id: Long, updatedDelivery: Delivery): Delivery? {
        return if (deliveryRepository.existsById(id)) {
            deliveryRepository.save(updatedDelivery.copy(id = id))
        } else null
    }
    
    fun deleteDelivery(id: Long): Boolean {
        return if (deliveryRepository.existsById(id)) {
            deliveryRepository.deleteById(id)
            true
        } else false
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
    
    fun assignDeliveryToDriver(deliveryId: Long, driverId: Long): Delivery? {
        val delivery = deliveryRepository.findById(deliveryId).orElse(null) ?: return null
        val driver = driverRepository.findById(driverId).orElse(null) ?: return null
        
        val updatedDelivery = delivery.copy(
            driver = driver,
            status = DeliveryStatus.ASSIGNED
        )
        
        return deliveryRepository.save(updatedDelivery)
    }
    
    fun completeDelivery(deliveryId: Long): Delivery? {
        val delivery = deliveryRepository.findById(deliveryId).orElse(null) ?: return null
        
        val updatedDelivery = delivery.copy(
            status = DeliveryStatus.COMPLETED,
            completedAt = java.time.LocalDateTime.now()
        )
        
        return deliveryRepository.save(updatedDelivery)
    }
}