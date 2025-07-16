package com.logistics.service

import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.repository.DriverRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DriverService(
    private val driverRepository: DriverRepository
) {
    fun getAllDrivers(): List<Driver> = driverRepository.findAll()
    
    fun getDriverById(id: Long): Driver? = driverRepository.findById(id).orElse(null)
    
    fun createDriver(driver: Driver): Driver = driverRepository.save(driver)
    
    fun updateDriver(id: Long, updatedDriver: Driver): Driver? {
        return if (driverRepository.existsById(id)) {
            driverRepository.save(updatedDriver.copy(id = id))
        } else null
    }
    
    fun deleteDriver(id: Long): Boolean {
        return if (driverRepository.existsById(id)) {
            driverRepository.deleteById(id)
            true
        } else false
    }
    
    fun getActiveDrivers(): List<Driver> = driverRepository.findByStatus(DriverStatus.ACTIVE)
    
    fun getAvailableDriversForDate(date: LocalDate): List<Driver> = 
        driverRepository.findAvailableDriversForDate(date)
}