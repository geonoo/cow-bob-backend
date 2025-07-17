package com.logistics.controller

import com.logistics.dto.DriverRequestDto
import com.logistics.dto.DriverResponseDto
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
    fun getAllDrivers(): List<DriverResponseDto> =
        driverService.getAllDrivers().map { DriverResponseDto.fromEntity(it) }
    
    @GetMapping("/{id}")
    fun getDriverById(@PathVariable id: Long): DriverResponseDto {
        return DriverResponseDto.fromEntity(driverService.getDriverById(id))
    }
    
    @PostMapping
    fun createDriver(@RequestBody dto: DriverRequestDto): ResponseEntity<DriverResponseDto> {
        val created = driverService.createDriver(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(DriverResponseDto.fromEntity(created))
    }
    
    @PutMapping("/{id}")
    fun updateDriver(@PathVariable id: Long, @RequestBody dto: DriverRequestDto): DriverResponseDto {
        return DriverResponseDto.fromEntity(driverService.updateDriver(id, dto))
    }
    
    @DeleteMapping("/{id}")
    fun deleteDriver(@PathVariable id: Long): ResponseEntity<Void> {
        driverService.deleteDriver(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/active")
    fun getActiveDrivers(): List<DriverResponseDto> =
        driverService.getActiveDrivers().map { DriverResponseDto.fromEntity(it) }
    
    @GetMapping("/available")
    fun getAvailableDrivers(@RequestParam date: String): List<DriverResponseDto> {
        val localDate = LocalDate.parse(date)
        return driverService.getAvailableDriversForDate(localDate).map { DriverResponseDto.fromEntity(it) }
    }
}