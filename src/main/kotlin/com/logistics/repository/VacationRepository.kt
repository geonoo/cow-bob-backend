package com.logistics.repository

import com.logistics.entity.Vacation
import com.logistics.entity.VacationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface VacationRepository : JpaRepository<Vacation, Long> {
    fun findByDriverIdAndStatus(driverId: Long, status: VacationStatus): List<Vacation>
    
    @Query("""
        SELECT v FROM Vacation v 
        WHERE v.driver.id = :driverId 
        AND v.status = 'APPROVED' 
        AND :date BETWEEN v.startDate AND v.endDate
    """)
    fun findActiveVacationForDriverAndDate(
        @Param("driverId") driverId: Long, 
        @Param("date") date: LocalDate
    ): Vacation?
}