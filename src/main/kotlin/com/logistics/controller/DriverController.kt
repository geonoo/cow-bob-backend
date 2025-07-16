package com.logistics.controller

import com.logistics.entity.Driver
import com.logistics.service.DriverService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = ["http://localhost:3000"])
class DriverController(
    private val driverService: DriverService
) {
    
    @GetMapping
    fun getAllDrivers(): List<Driver> = driverService.getAllDrivers()
    
    @GetMapping("/{id}")
    fun getDriverById(@PathVariable id: Long): Driver {
        return driverService.getDriverById(id)
    }
    
    @PostMapping
    fun createDriver(@RequestBody driver: Driver): ResponseEntity<Driver> {
        val createdDriver = driverService.createDriver(driver)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver)
    }
    
    @PutMapping("/{id}")
    fun updateDriver(@PathVariable id: Long, @RequestBody driver: Driver): Driver {
        return driverService.updateDriver(id, driver)
    }
    
    @DeleteMapping("/{id}")
    fun deleteDriver(@PathVariable id: Long): ResponseEntity<Void> {
        driverService.deleteDriver(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/active")
    fun getActiveDrivers(): List<Driver> = driverService.getActiveDrivers()
    
    @GetMapping("/available")
    fun getAvailableDrivers(@RequestParam date: String): List<Driver> {
        val localDate = LocalDate.parse(date)
        return driverService.getAvailableDriversForDate(localDate)
    }
}