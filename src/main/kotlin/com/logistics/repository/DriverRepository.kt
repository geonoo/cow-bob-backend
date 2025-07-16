package com.logistics.repository

import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface DriverRepository : JpaRepository<Driver, Long> {
    fun findByStatus(status: DriverStatus): List<Driver>
    
    @Query("""
        SELECT d FROM Driver d 
        WHERE d.status = 'ACTIVE' 
        AND d.id NOT IN (
            SELECT v.driver.id FROM Vacation v 
            WHERE v.status = 'APPROVED' 
            AND :date BETWEEN v.startDate AND v.endDate
        )
    """)
    fun findAvailableDriversForDate(@Param("date") date: LocalDate): List<Driver>
    
    @Query("""
        SELECT d, COUNT(del.id) as deliveryCount 
        FROM Driver d 
        LEFT JOIN d.deliveries del 
        WHERE d.status = 'ACTIVE' 
        GROUP BY d.id 
        ORDER BY deliveryCount ASC
    """)
    fun findDriversOrderByDeliveryCount(): List<Driver>
}