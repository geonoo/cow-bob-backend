package com.logistics.repository

import com.logistics.entity.Driver
import java.time.LocalDate

interface DriverRepositoryCustom {
    fun findAvailableDriversForDate(date: LocalDate): List<Driver>
    fun findDriversOrderByDeliveryCount(): List<Driver>
    fun findDriversByVehicleType(vehicleType: String): List<Driver>
    fun findDriversByTonnageRange(minTonnage: Double, maxTonnage: Double): List<Driver>
} 