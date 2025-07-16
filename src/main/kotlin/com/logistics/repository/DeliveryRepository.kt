package com.logistics.repository

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface DeliveryRepository : JpaRepository<Delivery, Long> {
    fun findByStatus(status: DeliveryStatus): List<Delivery>
    
    fun findByDriverIdAndStatus(driverId: Long, status: DeliveryStatus): List<Delivery>
    
    @Query("""
        SELECT COUNT(d) FROM Delivery d 
        WHERE d.driver.id = :driverId 
        AND d.destination = :destination 
        AND d.deliveryDate >= :fromDate
    """)
    fun countRecentDeliveriesForDriverAndDestination(
        @Param("driverId") driverId: Long,
        @Param("destination") destination: String,
        @Param("fromDate") fromDate: LocalDate
    ): Long
    
    @Query("""
        SELECT d.destination, COUNT(d) as count 
        FROM Delivery d 
        WHERE d.driver.id = :driverId 
        GROUP BY d.destination 
        ORDER BY count DESC
    """)
    fun findDriverDeliveryStatsByDestination(@Param("driverId") driverId: Long): List<Array<Any>>
}