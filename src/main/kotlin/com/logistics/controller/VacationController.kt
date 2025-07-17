package com.logistics.controller

import com.logistics.dto.VacationRequestDto
import com.logistics.dto.VacationResponseDto
import com.logistics.entity.Vacation
import com.logistics.service.VacationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vacations")
@CrossOrigin(origins = ["http://localhost:3000"])
class VacationController(
    private val vacationService: VacationService
) {
    
    @GetMapping
    fun getAllVacations(): List<VacationResponseDto> =
        vacationService.getAllVacations().map { VacationResponseDto.fromEntity(it) }
    
    @GetMapping("/{id}")
    fun getVacationById(@PathVariable id: Long): ResponseEntity<VacationResponseDto> {
        val vacation = vacationService.getVacationById(id)
        return if (vacation != null) {
            ResponseEntity.ok(VacationResponseDto.fromEntity(vacation))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createVacation(@RequestBody dto: VacationRequestDto): ResponseEntity<VacationResponseDto> {
        val created = vacationService.createVacation(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(VacationResponseDto.fromEntity(created))
    }
    
    @PutMapping("/{id}")
    fun updateVacation(@PathVariable id: Long, @RequestBody dto: VacationRequestDto): ResponseEntity<VacationResponseDto> {
        val updated = vacationService.updateVacation(id, dto)
        return if (updated != null) {
            ResponseEntity.ok(VacationResponseDto.fromEntity(updated))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteVacation(@PathVariable id: Long): ResponseEntity<Void> {
        return if (vacationService.deleteVacation(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/driver/{driverId}")
    fun getVacationsByDriverId(@PathVariable driverId: Long): List<VacationResponseDto> =
        vacationService.getVacationsByDriverId(driverId).map { VacationResponseDto.fromEntity(it) }
    
    @PostMapping("/{id}/approve")
    fun approveVacation(@PathVariable id: Long): ResponseEntity<VacationResponseDto> {
        val approved = vacationService.approveVacation(id)
        return if (approved != null) {
            ResponseEntity.ok(VacationResponseDto.fromEntity(approved))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{id}/reject")
    fun rejectVacation(@PathVariable id: Long): ResponseEntity<VacationResponseDto> {
        val rejected = vacationService.rejectVacation(id)
        return if (rejected != null) {
            ResponseEntity.ok(VacationResponseDto.fromEntity(rejected))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}