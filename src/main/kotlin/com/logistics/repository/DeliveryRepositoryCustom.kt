package com.logistics.repository

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import java.time.LocalDate
import java.time.YearMonth

interface DeliveryRepositoryCustom {
    fun findDeliveriesByStatus(status: DeliveryStatus): List<Delivery>
    fun findDeliveriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Delivery>
    fun findDeliveriesByDriverAndStatus(driverId: Long, status: DeliveryStatus): List<Delivery>
    fun findDeliveriesByDestination(destination: String): List<Delivery>
    fun countDeliveriesByStatus(status: DeliveryStatus): Long
    fun findRecentDeliveriesForDriverAndDestination(driverId: Long, destination: String, sinceDate: LocalDate): List<Delivery>
    fun findDeliveriesByDriverAndDateRange(driverId: Long, startDate: LocalDate, endDate: LocalDate): List<Delivery>
    fun findCompletedDeliveriesByDriverAndYearMonth(driverId: Long, yearMonth: YearMonth): List<Delivery>
    fun findCompletedDeliveriesByYearMonth(yearMonth: YearMonth): List<Delivery>
} 