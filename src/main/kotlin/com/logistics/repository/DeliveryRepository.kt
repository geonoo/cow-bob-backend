package com.logistics.repository

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DeliveryRepository : JpaRepository<Delivery, Long>, DeliveryRepositoryCustom {
    fun findByStatus(status: DeliveryStatus): List<Delivery>
    
    fun findByStatusIn(statuses: List<DeliveryStatus>): List<Delivery>
    
    fun findByDriverId(driverId: Long): List<Delivery>
    
    fun findByDriverIdAndStatus(driverId: Long, status: DeliveryStatus): List<Delivery>
    
    fun findByDeliveryDateBetween(startDate: LocalDate, endDate: LocalDate): List<Delivery>
}